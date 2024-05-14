package konsyst.ru.features.scenarios

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.events.*
import konsyst.ru.database.scenarios.*
import konsyst.ru.features.scenarios.models.FetchScenariosRequest
import konsyst.ru.features.scenarios.models.CreateScenarioRequest
import konsyst.ru.features.scenarios.models.FetchScenariosResponse
import konsyst.ru.features.scenarios.models.ScenarioResponse
import konsyst.ru.utils.TokenCheck
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

class ScenariosController {

    suspend fun Search(call: ApplicationCall) {
        val request = call.receive<FetchScenariosRequest>()
        val token = call.request.headers["Bearer-Authorization"]?.orEmpty()
        if (!TokenCheck.isTokenValid(token.toString()) && !TokenCheck.isTokenAdmin(token.toString())) {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
            return
        }

        val scenarioIds: List<Int> = EventScenarios.fetchScenarioIdsByEventName(request.searchQuery)

        val scenarios: List<ScenarioResponse> = transaction {
            Scenarios.select { Scenarios.id inList scenarioIds }
                .map { scenarioRow ->
                    ScenariosDataTransferObject(
                        id = scenarioRow[Scenarios.id],
                        title = scenarioRow[Scenarios.title],
                        description = scenarioRow[Scenarios.description],
                        date = scenarioRow[Scenarios.date],
                        location = scenarioRow[Scenarios.location],
                        isCompleted = scenarioRow[Scenarios.isCompleted],
                    ).mapToScenarioResponse()
                }
        }

        call.respond(FetchScenariosResponse(scenarios = scenarios))
    }

    suspend fun createScenario(call: ApplicationCall) {
        val token = call.request.headers["Bearer-Authorization"]
        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            val request = call.receive<CreateScenarioRequest>()
            val scenarioDTO = request.mapToScenarioDTO()

            val existingIds = Scenarios.fetchScenarios().map { it.id }.toSet()
            scenarioDTO.id = generateUniqueId(existingIds)

            Scenarios.insert(scenarioDTO)
            call.respond(scenarioDTO.mapToCreateScenarioResponse())
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }
    }

    private fun generateUniqueId(existingIds: Set<Int?>): Int {
        var uniqueId: Int
        do {
            uniqueId = Random.nextInt(Int.MAX_VALUE)
        } while (existingIds.contains(uniqueId))
        return uniqueId
    }
}
