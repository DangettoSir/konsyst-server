package konsyst.ru.features.events

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.events.*
import konsyst.ru.database.scenarios.Scenarios
import konsyst.ru.database.tokens.Tokens
import konsyst.ru.database.users.Users
import konsyst.ru.features.events.models.*
import konsyst.ru.utils.TokenCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

class EventsController {

    suspend fun Search(call: ApplicationCall) {
        val request = call.receive<FetchEventsRequest>()
        val token = call.request.headers["Bearer-Authorization"]?.orEmpty()

        if (!TokenCheck.isTokenValid(token.toString()) && !TokenCheck.isTokenAdmin(token.toString())) {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
            return
        }

        val login = fetchLoginFromTokenDatabase(token.toString())
        val userId = login?.let { fetchUserIdFromUserDatabase(it) }

        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, "User not found")
            return
        }

        val events: List<EventResponse> = transaction {
            Events.select { Events.userId eq userId }
                .map { eventRow ->
                    EventDataTransferObject(
                        id = eventRow[Events.id],
                        title = eventRow[Events.title],
                        date = eventRow[Events.date],
                        scenariosCount = eventRow[Events.scenariosCount],
                        scenariosComplete = eventRow[Events.scenariosComplete],
                        userId = eventRow[Events.userId],
                        status = EventStatus.valueOf(eventRow[Events.status])
                    ).mapToEventResponse()
                }
        }
        call.respond(FetchEventsResponse(events = events))
    }


    private suspend fun fetchLoginFromTokenDatabase(token: String): String? {
        return transaction {
            Tokens.select { Tokens.token eq token }
                .mapNotNull { it[Tokens.login] }
                .singleOrNull()
        }
    }

    private suspend fun fetchUserIdFromUserDatabase(login: String): Int? {
        return transaction {
            Users.select { Users.login eq login }
                .map { it[Users.id] }
                .singleOrNull()
        }
    }

    suspend fun getEventScenarios(call: ApplicationCall) {
        val eventId = call.parameters["eventId"]?.toIntOrNull() ?: return call.respond(HttpStatusCode.BadRequest)
        val scenarioIds = EventScenarios.fetchScenarioIds(eventId)
        val scenarios = Scenarios.fetchScenariosIds(scenarioIds)
        call.respond(scenarios)
    }

    suspend fun createEvent(call: ApplicationCall) {
        val token = call.request.headers["Bearer-Authorization"]
        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            val request = call.receive<CreateEventRequest>()
            val event = request.mapToEventDTO()

            val existingIds = Events.fetchEvents().map { it.id }.toSet()
            event.id = generateUniqueId(existingIds)

            Events.insert(event)
            call.respond(event.mapToCreateEventResponse())
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }
    }

    fun checkEventInEventScenarios(eventId: Int): Boolean {
        return transaction {
            val existingEvent = EventScenarios.select { EventScenarios.eventId eq eventId }.firstOrNull()
            existingEvent != null
        }
    }

    suspend fun linkScenarios(call: ApplicationCall) = withContext(Dispatchers.IO) {
        val token = call.request.headers["Bearer-Authorization"]
        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            val request = call.receive<LinkScenariosRequest>()
            val eventId = request.eventId
            val newScenarioIds = request.scenarioIds.toSet()

            transaction {
                // Получаем существующие связи между событием и сценариями
                val existingScenarioIds = EventScenarios.fetchScenarioIds(eventId).toMutableSet()

                // Добавляем новые сценарии, которые еще не связаны с этим событием
                val scenariosToAdd = newScenarioIds - existingScenarioIds
                scenariosToAdd.forEach { scenarioId ->
                    EventScenarios.insert {
                        it[EventScenarios.eventId] = eventId
                        it[EventScenarios.scenarioId] = scenarioId
                    }
                }
            }

            // Получаем обновленный список связей после добавления новых
            val updatedScenarioIds = EventScenarios.fetchScenarioIds(eventId)

            // Отправляем ответ внутри той же корутины
            call.respond(HttpStatusCode.OK, "Scenarios $updatedScenarioIds linked to event $eventId")
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }
    }








    fun generateUniqueId(existingIds: Set<Int?>): Int {
        var uniqueId: Int
        do {
            uniqueId = Random.nextInt(Int.MAX_VALUE)
        } while (existingIds.contains(uniqueId))
        return uniqueId
    }
}
