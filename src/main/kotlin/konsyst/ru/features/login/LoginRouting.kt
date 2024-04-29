package konsyst.ru.features.login

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import konsyst.ru.cache.InMemoryCache
import konsyst.ru.cache.TokenCache
import java.util.*

fun Application.configureLoginRouting() {
    val loginController = LoginController()
    routing {
        post("/login") {
            loginController.loginExecute(call)
        }
    }
}