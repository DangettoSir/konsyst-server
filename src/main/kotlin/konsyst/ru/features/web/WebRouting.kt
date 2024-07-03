package konsyst.ru.features.web

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.io.encoding.ExperimentalEncodingApi

@ExperimentalEncodingApi
fun Application.configureWebRouting() {
    val webController = WebController()
    routing {
        get("/web/search") {
            webController.fetchEventWithScenarios(call)
        }
        post("/web/scenario/search") {
            webController.fetchScenario(call)
        }
        get("/web/users"){
            webController.fetchListUsers(call)
        }
        post("/web/getsteps"){
            webController.fetchStepsForReport(call)
        }
    }
}