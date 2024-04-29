package konsyst.ru.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import java.util.*


fun Application.configureRouting() {
    routing {
        get("/") {
            val clientIP = call.request.origin.remoteAddress
            println("client IP: $clientIP")
            call.respondText( "Hello, World")
        }
        get("/test"){
            call.respondText("Test Hello World :)")
        }
    }
}
