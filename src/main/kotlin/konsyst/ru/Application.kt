package konsyst.ru

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import konsyst.ru.features.login.configureLoginRouting
import konsyst.ru.features.events.configureEventsRouting
import konsyst.ru.features.register.configureRegisterRouting
import konsyst.ru.plugins.*
import org.jetbrains.exposed.sql.Database

fun main() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/konsyst",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "fmn!-4737jnbs"
    )
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
    configureRegisterRouting()
    configureLoginRouting()
    configureEventsRouting()
    configureSockets()
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureRouting()
}
