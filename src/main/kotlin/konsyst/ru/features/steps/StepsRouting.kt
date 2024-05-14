package konsyst.ru.features.steps

import io.ktor.server.application.*
import io.ktor.server.routing.*


fun Application.configureStepsRouting() {
    val stepsController = StepsController()
    routing {
        post("/steps/add") {
            stepsController.createStep(call)
        }
    }
}