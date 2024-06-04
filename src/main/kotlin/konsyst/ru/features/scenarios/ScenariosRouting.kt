package konsyst.ru.features.scenarios

import io.ktor.server.application.*
import io.ktor.server.routing.*


fun Application.configureScenariosRouting() {
    val scenariosController = ScenariosController()
    routing {
        post("/scenarios/add") {
            scenariosController.createScenario(call)
        }
        post("/scenarios/search") {
            scenariosController.Search(call)
        }
        post("/scenarios/search-all"){
            scenariosController.SearchAll(call)
        }
        post("/scenarios/link-steps") {
            scenariosController.linkSteps(call)
        }
        post("/scenarios/update"){
            scenariosController.updateScenarioStatus(call)
        }
    }
}