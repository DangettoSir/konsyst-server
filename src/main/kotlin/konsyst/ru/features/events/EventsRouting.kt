package konsyst.ru.features.events

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.io.encoding.ExperimentalEncodingApi

@ExperimentalEncodingApi
fun Application.configureEventsRouting() {
    val eventsController = EventsController()
    routing {
        post("/events/addUser"){
            eventsController.EventAddUser(call)
        }
        post("/events/add") {
            eventsController.createEvent(call)
        }
        post("/events/search"){
            eventsController.Search(call)
        }
        post("/events/link-scenarios") {
            eventsController.linkScenarios(call)
        }
        post("/events/status") {
            eventsController.getEventCompleteStatus(call)
        }
        post("/events/statusUpdate") {
            eventsController.updateEventStatusCall(call)
        }
        get("/events/searchAll") {
            eventsController.fetchEvents(call)
        }
        get("/events/get-linked-scenarios"){
            eventsController.getEventScenarios(call)
        }
    }
}