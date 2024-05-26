package konsyst.ru.features.login


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import konsyst.ru.database.tokens.JwtConfig
import kotlin.io.encoding.ExperimentalEncodingApi

@ExperimentalEncodingApi
fun Application.configureLoginRouting(jwtConfig: JwtConfig) {
    val loginController = LoginController(jwtConfig)

    routing {
        post("/login") {
            loginController.loginExecute(call)
        }
        authenticate {
            get("/protected") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                val login = principal?.payload?.getClaim("login")?.asString()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (login != null && role != null) {
                    call.respondText("Hello, $username with role $role")
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}