package konsyst.ru

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import konsyst.ru.database.tokens.JwtConfig
import konsyst.ru.features.events.configureEventsRouting
import konsyst.ru.features.login.configureLoginRouting
import konsyst.ru.features.notifications.NotificationsController
import konsyst.ru.features.register.configureRegisterRouting
import konsyst.ru.features.scenarios.configureScenariosRouting
import konsyst.ru.features.steps.configureStepsRouting
import konsyst.ru.features.userdata.configureUserDataRouting
import konsyst.ru.plugins.configureHTTP
import konsyst.ru.plugins.configureRouting
import konsyst.ru.plugins.configureSecurity
import konsyst.ru.plugins.configureSerialization
import org.jetbrains.exposed.sql.Database
import kotlin.io.encoding.ExperimentalEncodingApi

@ExperimentalEncodingApi
fun main() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/konsyst",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "fmn!-4737jnbs"
    )
    embeddedServer(Netty, port = 5085, host = "192.168.0.102", module = Application::module)
        .start(wait = true)
}
val jwtConfig = JwtConfig(
    issuer = "Konsyst",
    audience = "KonsystSecureApp",
    secret = "fmn!-4737jnbs",
    expirationTimeInMillis = 3600000,
    roles = mapOf(
        "admin" to "ROLE_ADMIN",
        "support" to "ROLE_SUPPORT",
        "superadmin" to "ROLE_SUPERADMIN",
        "user" to "ROLE_USER"
    )
)

@ExperimentalEncodingApi
fun Application.module() {
    install(Authentication) {
        jwt {
            verifier(jwtConfig.createJwtVerifier())
            validate { credential ->
                val login = credential.payload.getClaim("login").asString()
                val role = credential.payload.getClaim("role")?.asString()
                if (login.isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    val notificationsController = NotificationsController(environment)
    configureRouting()
    configureRegisterRouting()
    configureUserDataRouting()
    configureLoginRouting(jwtConfig)
    configureEventsRouting()
    NotificationsController()
    configureScenariosRouting()
    configureStepsRouting()
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureRouting()
}
