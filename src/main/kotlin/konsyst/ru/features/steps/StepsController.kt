package konsyst.ru.features.steps

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.steps.Steps
import konsyst.ru.database.steps.mapToCreateStepResponse
import konsyst.ru.database.steps.mapToStepDTO
import konsyst.ru.features.steps.models.CreateStepRequest
import konsyst.ru.utils.TokenCheck

class StepsController {
    suspend fun createStep(call: ApplicationCall){
        val token = call.request.headers["Bearer-Authorization"]
        if(TokenCheck.isTokenAdmin(token.orEmpty())){
            val request = call.receive<CreateStepRequest>()
            val event = request.mapToStepDTO()
            Steps.insert(event)
            call.respond(event.mapToCreateStepResponse())
        } else {
            call.respond(HttpStatusCode.Unauthorized,"Token expired")
        }
    }
}