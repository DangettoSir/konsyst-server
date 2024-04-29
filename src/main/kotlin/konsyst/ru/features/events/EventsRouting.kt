package konsyst.ru.features.events

import io.ktor.server.application.*
import io.ktor.server.routing.*


fun Application.configureEventsRouting() {
    val eventsController = EventsController()
    routing {
        post("/events/add") {
            eventsController.createEvent(call)
        }
        post("/events/search"){
            eventsController.Search(call)
        }
    }
}