package konsyst.ru.features.events

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import konsyst.ru.cache.InMemoryCache
import konsyst.ru.cache.TokenCache
import java.util.*

fun Application.configureGamesRouting() {
    routing {
        post("/events/fetch") {
            val  eventsController = EventsController(call)
            eventsController.fetchAllEvents()
        }
        post("events/add"){
            val eventsController = EventsController(call)
            eventsController.addEvent()
        }
    }
}