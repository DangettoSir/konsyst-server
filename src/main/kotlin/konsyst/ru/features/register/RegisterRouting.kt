package konsyst.ru.features.register

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.io.encoding.ExperimentalEncodingApi

@ExperimentalEncodingApi
fun Application.configureRegisterRouting() {
    val registerController = RegisterController()
    routing {
        post("/register") {
            registerController.registerNewUser(call)
        }
    }
}

