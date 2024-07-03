package konsyst.ru.features.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.events.EventDataTransferObject
import konsyst.ru.database.events.EventScenarios
import konsyst.ru.database.events.EventStatus
import konsyst.ru.database.events.Events
import konsyst.ru.database.scenarios.ScenarioSteps
import konsyst.ru.database.scenarios.Scenarios
import konsyst.ru.database.scenarios.ScenariosDataTransferObject
import konsyst.ru.database.steps.Steps
import konsyst.ru.database.steps.Steps.fetchStepsByIds
import konsyst.ru.database.userdata.UserDataSteps.fetchUserCommentByLogin
import konsyst.ru.database.users.Users.fetchUserNameById
import konsyst.ru.database.users.Users.fetchUsers
import konsyst.ru.database.users.Users.fetchUsersNamesById
import konsyst.ru.features.scenarios.models.FetchScenarioResponse
import konsyst.ru.features.scenarios.models.FetchScenariosRequest
import konsyst.ru.features.steps.models.StepsResponse
import konsyst.ru.features.web.models.FetchReportsResponse
import konsyst.ru.features.web.models.WebModel
import konsyst.ru.utils.TokenCheck
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class WebController {

    suspend fun fetchStepsForReport(call: ApplicationCall){
        val id = call.receive<WebModel>()
        val token = call.request.headers["Bearer-Authorization"]
        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            val steps = fetchStepsByIds(id.stepsId)
            val user = fetchUsersNamesById(id.userId)
            call.respond(FetchReportsResponse(userName = user, steps = steps))
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }


    suspend fun fetchListUsers(call: ApplicationCall){
        val token = call.request.headers["Bearer-Authorization"]
        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            val users = fetchUsers()
            call.respond(users)
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
    suspend fun fetchScenario(call: ApplicationCall) {
        val request = call.receive<FetchScenariosRequest>()
        val token = call.request.headers["Bearer-Authorization"]

        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            val stepsIds: List<Int> = ScenarioSteps.fetchStepIdsByScenarioName(request.searchQuery)

            val steps: List<StepsResponse> = transaction {
                Steps.select { Steps.id inList stepsIds }
                    .map { stepRow ->
                        val photoChecked = stepRow[Steps.action].contains("photo")
                        val videoChecked = stepRow[Steps.action].contains("video")
                        val allChecked = stepRow[Steps.action].contains("photo,video")
                        StepsResponse(
                            id = stepRow[Steps.id],
                            title = stepRow[Steps.title],
                            scenarioId = stepRow[Steps.scenarioId],
                            action = stepRow[Steps.action],
                            number = stepRow[Steps.number],
                            html = """
                            <div class="col-md-6 mt-3 step" id="${stepRow[Steps.id]}">
                              <div class="card">
                                <div class="card-body">
                                  <h5 class="card-title">Шаг № ${stepRow[Steps.number]}</h5>
                                  <h6 class="card-subtitle mb-2 text-muted">${stepRow[Steps.title]}</h6>
                                  <p class="card-text">Тип отчетности</p>
                                  <div class="d-flex justify-content-between align-items-center">
                                    <div class="d-flex align-items-center">
                                      <img src="../assets/icons/photo.svg" style="width: 20px; height: 20px;" class="me-2">
                                      Фото
                                      <div class="form-check form-switch ms-3">
                                        <input class="form-check-input" type="checkbox" id="photo" ${if (photoChecked) "checked" else if (allChecked) "checked" else ""}>
                                      </div>
                                      <img src="../assets/icons/video.svg" style="width: 20px; height: 20px;" class="ms-3 me-2">
                                      Видео
                                      <div class="form-check form-switch ms-3">
                                        <input class="form-check-input" type="checkbox" id="video" ${if (videoChecked) "checked" else if (allChecked) "checked" else ""}>
                                      </div>
                                    </div>
                                  </div>
                                </div>  
                                <div class="card-footer d-flex">
                                  <button class="btn btn-primary btn-sm me-3">Изменить</button>
                                  <button class="btn btn-danger btn-sm">Удалить</button>
                                </div>
                              </div>
                            </div>
                        """.trimIndent()
                        )
                    }
            }

            val scenario = Scenarios.fetchScenario(request.searchQuery)

            call.respond(
                FetchScenarioResponse(
                    scenario = scenario,
                    steps = steps
                )
            )
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
    @Serializable
    data class EventWithScenarios(
        val eventDto: EventDataTransferObject,
        val scenarios: List<ScenariosDataTransferObject>,
    )

    suspend fun fetchEventWithScenarios(call: ApplicationCall) {
        val token = call.request.headers["Bearer-Authorization"]
        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            val events: List<EventWithScenarios> = transaction {
                Events.selectAll()
                    .map { eventRow ->
                        val eventId = eventRow[Events.id]
                        val scenarioIds = EventScenarios.fetchScenarioIds(eventId)
                        val scenarios = Scenarios.fetchScenariosIds(scenarioIds)
                            .map { scenarioRow ->
                                ScenariosDataTransferObject(
                                    id = scenarioRow.id,
                                    title = scenarioRow.title,
                                    description = scenarioRow.description,
                                    date = scenarioRow.date,
                                    location = scenarioRow.location,
                                    isCompleted = scenarioRow.isCompleted
                                )
                            }
                        val userId = eventRow[Events.userId]
                        val userName = fetchUserNameById(userId)

                        val userFirstAndSecond = userName?.let { getUserFirstAndSecond(it) }
                        val comment = fetchUserCommentByLogin(userId)
                        EventWithScenarios(
                            eventDto = EventDataTransferObject(
                                id = eventRow[Events.id],
                                title = eventRow[Events.title],
                                date = eventRow[Events.date],
                                scenariosCount = eventRow[Events.scenariosCount],
                                scenariosComplete = eventRow[Events.scenariosComplete],
                                userId = eventRow[Events.userId],
                                status = EventStatus.valueOf(eventRow[Events.status]),
                                comment = comment,
                                userName = userName
                            ),
                            scenarios = scenarios
                        )
                    }
            }.sortedByDescending { it.eventDto.scenariosCount }


            val eventHtml = buildString {
                events.forEach { event ->
                    append("""
            <div class="event" id="${event.eventDto.id}">
                <h4 class="eventtitle p-3">${event.eventDto.title}
                    <img class="eventsettings" src="../assets/icons/eventmore.svg" alt="EventMore" id="${event.eventDto.id}">
                </h4>
        """)


                    if (event.scenarios.isNotEmpty()) {
                        append("""
                <div class="scenarios">
                    ${event.scenarios.joinToString("") { scenario ->
                            """
                        <div class="scenario p-3 m-3 mb-4" id="${scenario.id}">
                            <h6 class="title">${scenario.title}</h6>
                            <div class="info mt-2 d-flex flex-wrap">
                                <div class="dates-container d-flex me-3">
                                    <img class="me-1 time" alt="Time">
                                    <span class="date">${scenario.date}</span>
                                </div>
                                <div class="comments-container d-flex me-3">
                                    <img class="me-1 comments" alt="Comments">
                                    <span class="comment">${if (event.eventDto.comment?.isNotEmpty() == true) "1" else ""}</span>
                                </div>
                                <div class="attachments-container d-flex ">
                                    <img class="me-1 attachment" alt="Attachments">
                                    <span class="attachment"></span>
                                </div>
                            </div>
                            <div class="users mt-2 d-flex">
                                <div class="useravatars me-2 d-flex justify-content-center align-items-center">
                                    <span class="useravatitle">${getUserFirstAndSecond(event.eventDto.userName)}</span>
                                </div>
                            </div>
                        </div>
                        """
                        }}
                </div>
            """)
                    }

                    append("""
            <div class="d-flex justify-content-center mb-3">
                <button class="btn adds" onclick="showModal(event)" id="${event.eventDto.id}">
                    <span>
                        <img class="addicon me-1" src="../assets/icons/add.svg" alt="Add scenario">
                        <span class="addtext">Добавить сценарий</span>
                    </span>
                </button>
            </div>
        """)

                    append("</div>")

                }
                append("""
                        <div class="event flex-grow-1 me-md-3 mb-3 mb-md-0">
                          <div class="d-flex justify-content-center">
                            <button class="btn" onclick="showAddEvent()">
                              <span>
                                <img class="addicon me-1" src="../assets/icons/add.svg" alt="Add scenario">
                                <span class="addtext">Добавить мероприятие</span>
                              </span>
                            </button>
                          </div>
                        </div>
                    """)
            }
            
            call.respondText(eventHtml, ContentType.Text.Html)
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }





        suspend fun fetchReports(call: ApplicationCall) {
            val request = call.receive<FetchScenariosRequest>()
            val token = call.request.headers["Bearer-Authorization"]

            if (TokenCheck.isTokenAdmin(token.orEmpty())) {
                val stepsIds: List<Int> = ScenarioSteps.fetchStepIdsByScenarioName(request.searchQuery)

                val steps: List<StepsResponse> = transaction {
                    Steps.select { Steps.id inList stepsIds }
                        .map { stepRow ->
                            val photoChecked = stepRow[Steps.action].contains("photo")
                            val videoChecked = stepRow[Steps.action].contains("video")
                            val allChecked = stepRow[Steps.action].contains("photo,video")
                            StepsResponse(
                                id = stepRow[Steps.id],
                                title = stepRow[Steps.title],
                                scenarioId = stepRow[Steps.scenarioId],
                                action = stepRow[Steps.action],
                                number = stepRow[Steps.number],
                                html = """
                            <div class="col-md-6 mt-3 step" id="${stepRow[Steps.id]}">
                              <div class="card">
                                <div class="card-body">
                                  <h5 class="card-title">Шаг № ${stepRow[Steps.number]}</h5>
                                  <h6 class="card-subtitle mb-2 text-muted">${stepRow[Steps.title]}</h6>
                                  <p class="card-text">Тип отчетности</p>
                                  <div class="d-flex justify-content-between align-items-center">
                                    <div class="d-flex align-items-center">
                                      <img src="../assets/icons/photo.svg" style="width: 20px; height: 20px;" class="me-2">
                                      Фото
                                      <div class="form-check form-switch ms-3">
                                        <input class="form-check-input" type="checkbox" id="photo" ${if (photoChecked) "checked" else if (allChecked) "checked" else ""}>
                                      </div>
                                      <img src="../assets/icons/video.svg" style="width: 20px; height: 20px;" class="ms-3 me-2">
                                      Видео
                                      <div class="form-check form-switch ms-3">
                                        <input class="form-check-input" type="checkbox" id="video" ${if (videoChecked) "checked" else if (allChecked) "checked" else ""}>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                                <div class="card-footer d-flex">
                                  <button class="btn btn-primary btn-sm me-3">Изменить</button>
                                  <button class="btn btn-danger btn-sm">Удалить</button>
                                </div>
                              </div>
                            </div>
                        """.trimIndent()
                            )
                        }
                }

                val scenario = Scenarios.fetchScenario(request.searchQuery)

                call.respond(
                    FetchScenarioResponse(
                        scenario = scenario,
                        steps = steps
                    )
                )
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }


    }

    private fun getUserFirstAndSecond(userName: String?): String {
        val parts = userName?.split(" ")
        val firstName = parts?.firstOrNull()?.firstOrNull()?.toString() ?: ""
        val lastName = parts?.lastOrNull()?.firstOrNull()?.toString() ?: ""
        return "$firstName$lastName"
    }
}