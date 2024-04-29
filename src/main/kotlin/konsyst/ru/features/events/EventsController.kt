package konsyst.ru.features.events

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.events.Events
import konsyst.ru.database.events.mapToCreateEventResponse
import konsyst.ru.database.events.mapToEventDTO
import konsyst.ru.database.events.mapToEventResponse
import konsyst.ru.features.events.models.FetchEventsRequest
import konsyst.ru.features.events.models.CreateEventRequest
import konsyst.ru.features.events.models.FetchEventsResponse
import konsyst.ru.utils.TokenCheck

class EventsController {

    suspend fun Search(call: ApplicationCall){
        val request = call.receive<FetchEventsRequest>()
        val token = call.request.headers["Bearer-Authorization"]
        if(TokenCheck.isTokenValid(token.orEmpty()) || TokenCheck.isTokenAdmin(token.orEmpty())){
            call.respond(FetchEventsResponse(
                events = Events.fetchEvents().filter {it.eventName.contains(request.searchQuery, ignoreCase = true)}
                    .map {it.mapToEventResponse()}
            ))
        } else{
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }
    }

    suspend fun createEvent(call: ApplicationCall){
        val token = call.request.headers["Bearer-Authorization"]
        if(TokenCheck.isTokenAdmin(token.orEmpty())){
            val request = call.receive<CreateEventRequest>()
            val event = request.mapToEventDTO()
            Events.insert(event)
            call.respond(event.mapToCreateEventResponse())
        } else {
            call.respond(HttpStatusCode.Unauthorized,"Token expired")
        }
    }
}