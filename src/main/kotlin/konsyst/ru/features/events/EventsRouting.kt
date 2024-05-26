package konsyst.ru.features.events

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.io.encoding.ExperimentalEncodingApi

@ExperimentalEncodingApi
fun Application.configureEventsRouting() {
    val eventsController = EventsController()
    routing {
        post("/events/add") {
            eventsController.createEvent(call)
        }
        post("/events/search"){
            eventsController.Search(call)
        }
        post("/events/link-scenarios") {
            eventsController.linkScenarios(call)
        }
        get("/events/get-linked-scenarios"){
            eventsController.getEventScenarios(call)
        }
    }
}