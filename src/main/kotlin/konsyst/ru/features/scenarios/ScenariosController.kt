package konsyst.ru.features.scenarios

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.events.EventScenarios
import konsyst.ru.database.events.Events
import konsyst.ru.database.scenarios.*
import konsyst.ru.database.scenarios.Scenarios.updateStatus
import konsyst.ru.database.tokens.Tokens
import konsyst.ru.database.users.Users
import konsyst.ru.features.scenarios.models.*
import konsyst.ru.utils.TokenCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random

class ScenariosController {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    suspend fun Search(call: ApplicationCall) {
        val request = call.receive<FetchScenariosRequest>()
        val token = call.request.headers["Bearer-Authorization"]?.orEmpty()
        if (!TokenCheck.isTokenValid(token.toString()) && !TokenCheck.isTokenAdmin(token.toString())) {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
            return
        }
        val scenarioIds: List<Int> = EventScenarios.fetchScenarioIdsByEventName(request.searchQuery)
        val eventName: String? = Events.fetchEventTitleByEventId(request.searchQuery)
        logger.debug("Fetched Event Name: '$eventName'")
        val scenarios: List<ScenarioResponse> = transaction {
            Scenarios.select { Scenarios.id inList scenarioIds }
                .map { scenarioRow ->
                    ScenariosDataTransferObject(
                        id = scenarioRow[Scenarios.id],
                        title = scenarioRow[Scenarios.title],
                        description = scenarioRow[Scenarios.description],
                        date = scenarioRow[Scenarios.date],
                        location = scenarioRow[Scenarios.location],
                        isCompleted = scenarioRow[Scenarios.isCompleted],
                        eventFrom = eventName
                    ).mapToScenarioResponse()
                }
        }

        call.respond(FetchScenariosResponse(scenarios = scenarios))
    }

    suspend fun SearchAll(call: ApplicationCall) {
        val token = call.request.headers["Bearer-Authorization"]?.orEmpty()
        logger.debug("Received token: '$token'")

        if (!TokenCheck.isTokenValid(token.toString()) && !TokenCheck.isTokenAdmin(token.toString())) {
            logger.debug("Token is not valid or not admin")
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
            return
        }


        val login = fetchLoginFromTokenDatabase(token.toString())
        logger.debug("Fetched login: '$login'")

        val userId = login?.let { fetchUserIdFromUserDatabase(it) }
        logger.debug("Fetched user ID: '$userId'")

        if (userId == null) {
            logger.debug("User not found")
            call.respond(HttpStatusCode.Unauthorized, "User not found")
            return
        }

        logger.debug("Fetching all event scenarios")
        val eventScenarios: List<EventScenarios.EventScenario> = transaction {
            EventScenarios.fetchAllEventScenarios()
        }
        val eventNames: List<Events.EventName> = transaction {
            Events.fetchAllEventTitles()
        }
        logger.debug("Fetched {} event scenarios", eventScenarios.size)

        val tableHeader = "| eventID | scenarioId |"
        val tableSeparator = "|---------+------------|"
        val tableRows = eventScenarios.joinToString("\n") { "| ${it.eventId} | ${it.scenarioId} |" }
        logger.debug("\n$tableHeader\n$tableSeparator\n$tableRows")
        val events = FetchEventsIdsByUserId(userId.toString())
        logger.debug("Events: [{}]", events?.joinToString(", "))
        logger.debug("Fetched {} event(s) for user {}", events?.let { 1 } ?: 0, userId)
        val uniqueEvents = events?.map { it }?.toSet() ?: emptySet()
        val matchingEventIds = eventScenarios
            .map { it.eventId }
            .distinct()
            .filter { it in uniqueEvents }
        val matchingEventNameIds = eventNames
            .map { it.id }
            .distinct()
            .filter { it in uniqueEvents }
        val matchingEventNames = eventNames
            .filter { it.id in matchingEventNameIds }
            .map { it.title }
        logger.debug("Found {} matching event IDs", matchingEventIds.size)
        logger.debug("Matching event IDs are: [{}]", matchingEventIds.joinToString(", "))
        val matchingScenarioIds = eventScenarios
            .filter { it.eventId in matchingEventIds }
            .map { it.scenarioId }
        logger.debug("Found {} matching scenario IDs", matchingScenarioIds.size)
        logger.debug("Matching scenario IDs are: [{}]", matchingScenarioIds.joinToString(", "))
        logger.debug("Fetching scenarios for IDs: {}", matchingScenarioIds)
        val scenarios: List<ScenarioResponse> = transaction {
            matchingScenarioIds.zip(matchingEventNames) { scenarioId, eventName ->
                Scenarios.select { Scenarios.id eq scenarioId }
                    .map { scenarioRow ->
                        ScenariosDataTransferObject(
                            id = scenarioRow[Scenarios.id],
                            title = scenarioRow[Scenarios.title],
                            description = scenarioRow[Scenarios.description],
                            date = scenarioRow[Scenarios.date],
                            location = scenarioRow[Scenarios.location],
                            isCompleted = scenarioRow[Scenarios.isCompleted],
                            eventFrom = eventName
                        ).mapToScenarioResponse()
                    }
            }.flatten()
        }
        logger.debug("Fetched {} scenarios", scenarios.size)

        call.respond(FetchScenariosResponse(scenarios = scenarios))
    }
    suspend fun getEventScenarios(call: ApplicationCall) {
        val eventId = call.parameters["eventId"]?.toIntOrNull() ?: return call.respond(HttpStatusCode.BadRequest)
        val scenarioIds = EventScenarios.fetchScenarioIds(eventId)
        val scenarios = Scenarios.fetchScenariosIds(scenarioIds)
        call.respond(scenarios)
    }
    suspend fun updateScenarioStatus(call: ApplicationCall) {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)

        val request = try {
            call.receive<UpdateStatus>()
        } catch (e: Exception) {
            logger.error("Error receiving UpdateStatus request: $e")
            call.respond(HttpStatusCode.BadRequest, "Invalid request")
            return
        }

        val token = call.request.headers["Bearer-Authorization"]?.orEmpty()
        logger.debug("Received token: '$token'")

        if (!TokenCheck.isTokenValid(token.toString()) && !TokenCheck.isTokenAdmin(token.toString())) {
            logger.debug("Token is not valid or not admin")
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
            return
        }

        logger.info("Received UpdateStatus request: $request")

        val isUpdated = try {
            updateStatus(request.id, request.eventId, request.isCompleted)
        } catch (e: Exception) {
            logger.error("Error updating scenario status: $e")
            false
        }

        if (isUpdated) {
            logger.info("Scenario status updated successfully")
            call.respond(HttpStatusCode.OK, "Scenario status updated")
        } else {
            logger.error("Failed to update scenario status")
            call.respond(HttpStatusCode.InternalServerError, "Failed to update scenario status")
        }
    }
    private fun FetchEventsIdsByUserId(userId: String): List<Int>? {
        return transaction {
            Events.select { Events.userId eq userId.toInt() }
                .map { it[Events.id] }
        }
    }

    suspend fun createScenario(call: ApplicationCall) {
        val token = call.request.headers["Bearer-Authorization"]
        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            val request = call.receive<CreateScenarioRequest>()
            val scenarioDTO = request.mapToScenarioDTO()

            val existingIds = Scenarios.fetchScenarios().map { it.id }.toSet()
            scenarioDTO.id = generateUniqueId(existingIds)

            Scenarios.insert(scenarioDTO)
            call.respond(scenarioDTO.mapToCreateScenarioResponse())
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }
    }

    suspend fun linkSteps(call: ApplicationCall) = withContext(Dispatchers.IO) {
        val token = call.request.headers["Bearer-Authorization"]
        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            val request = call.receive<LinkStepsRequest>()
            val scenarioId = request.scenarioId
            val newStepIds = request.stepIds.toSet()

            transaction {
                val existingStepIds = ScenarioSteps.fetchStepIds(scenarioId).toMutableSet()
                val stepsToAdd = newStepIds - existingStepIds
                stepsToAdd.forEach { stepId ->
                    ScenarioSteps.insert {
                        it[ScenarioSteps.scenarioId] = scenarioId
                        it[ScenarioSteps.stepId] = stepId
                    }
                }
            }
            val updatedStepIds = ScenarioSteps.fetchStepIds(scenarioId)
            call.respond(HttpStatusCode.OK, "Scenarios $updatedStepIds linked to scenario $scenarioId")
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }
    }

    suspend fun fetchScenarios(call: ApplicationCall) {
        val token = call.request.headers["Bearer-Authorization"]
        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            val scenarios: List<ScenarioResponse> = transaction {
                Scenarios.selectAll()
                    .map { scenarioRow ->
                            ScenariosDataTransferObject(
                                id = scenarioRow[Scenarios.id],
                                title = scenarioRow[Scenarios.title],
                                description = scenarioRow[Scenarios.description],
                                date = scenarioRow[Scenarios.date],
                                location = scenarioRow[Scenarios.location],
                                isCompleted = scenarioRow[Scenarios.isCompleted]
                            ).mapToScenarioResponse()
                        }
            }

            val scenarioHtml = buildString {
                scenarios.forEach { scenario ->
                    append("""
                            <div class="scenario p-3 m-3 mb-4">
                                <h6 class="title">${scenario.title}</h6>
                                <div class="info mt-2 d-flex flex-wrap">
                                    <div class="dates-container d-flex me-3">
                                        <img class="me-1 time" alt="Time">
                                        <span class="date">24 мар, 9:00</span>
                                    </div>
                                    <div class="comments-container d-flex me-3">
                                        <img class="me-1 comments" alt="Comments">
                                        <span class="comment">4</span>
                                    </div>
                                    <div class="attachments-container d-flex ">
                                        <img class="me-1 attachment" alt="Attachments">
                                        <span class="attachment">2</span>
                                    </div>
                                </div>
                                <div class="users mt-2 d-flex">
                                    <div class="useravatars me-2 d-flex justify-content-center align-items-center">
                                        <span class="useravatitle">АЕ</span>
                                    </div>
                                    <div class="useravatars me-2 d-flex justify-content-center align-items-center">
                                        <span class="useravatitle">АЕ</span>
                                    </div>
                                    <div class="useravatars me-2 d-flex justify-content-center align-items-center">
                                        <span class="useravatitle">АЕ</span>
                                    </div>
                                    <div class="useravatars me-2 d-flex justify-content-center align-items-center">
                                        <span class="useravatitle">АЕ</span>
                                    </div>
                                    <div class="useravatars me-2 d-flex justify-content-center align-items-center">
                                        <span class="useravatitle">АЕ</span>
                                    </div>
                                </div>
                            </div>
                """)
                }
            }

            call.respondText(scenarioHtml, ContentType.Text.Html)
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }
    }
    private suspend fun fetchLoginFromTokenDatabase(token: String): String? {
        return transaction {
            Tokens.select { Tokens.token eq token }
                .map { it[Tokens.login] }
                .firstOrNull()
        }
    }

    private suspend fun fetchUserIdFromUserDatabase(login: String): Int? {
        return transaction {
            Users.select { Users.login eq login }
                .map { it[Users.id] }
                .singleOrNull()
        }
    }

    private fun generateUniqueId(existingIds: Set<Int?>): Int {
        var uniqueId: Int
        do {
            uniqueId = Random.nextInt(Int.MAX_VALUE)
        } while (existingIds.contains(uniqueId))
        return uniqueId
    }
}


