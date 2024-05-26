package konsyst.ru.features.steps

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.scenarios.ScenarioSteps
import konsyst.ru.database.steps.*
import konsyst.ru.features.steps.models.CreateStepRequest
import konsyst.ru.features.steps.models.FetchStepsRequest
import konsyst.ru.features.steps.models.FetchStepsResponse
import konsyst.ru.features.steps.models.StepsResponse
import konsyst.ru.utils.TokenCheck
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

class StepsController {
    suspend fun Search(call: ApplicationCall) {
        val request = call.receive<FetchStepsRequest>()
        val token = call.request.headers["Bearer-Authorization"]?.orEmpty()
        if (!TokenCheck.isTokenValid(token.toString()) && !TokenCheck.isTokenAdmin(token.toString())) {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
            return
        }

        val stepsIds: List<Int> = ScenarioSteps.fetchStepIdsByScenarioName(request.searchQuery)

        val steps: List<StepsResponse> = transaction {
            Steps.select { Steps.id inList stepsIds }
                .map { stepRow ->
                    StepsDataTransferObject(
                        id = stepRow[Steps.id],
                        title = stepRow[Steps.title],
                        scenarioId = stepRow[Steps.scenarioId],
                        action = stepRow[Steps.action],
                        number = stepRow[Steps.number]
                    ).mapToStepResponse()
                }
        }

        call.respond(FetchStepsResponse(steps = steps))
    }

    suspend fun createStep(call: ApplicationCall){
        val token = call.request.headers["Bearer-Authorization"]
        if(TokenCheck.isTokenAdmin(token.orEmpty())){
            val request = call.receive<CreateStepRequest>()
            val step = request.mapToStepDTO()
            val existingIds = Steps.fetchSteps().map { it.id }.toSet()
            step.id = generateUniqueId(existingIds)
            Steps.insert(step)
            call.respond(step.mapToCreateStepResponse())
        } else {
            call.respond(HttpStatusCode.Unauthorized,"Token expired")
        }
    }


    fun generateUniqueId(existingIds: Set<Int?>): Int {
        var uniqueId: Int
        do {
            uniqueId = Random.nextInt(Int.MAX_VALUE)
        } while (existingIds.contains(uniqueId))
        return uniqueId
    }
}