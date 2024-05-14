package konsyst.ru.database.steps


import konsyst.ru.features.steps.models.CreateStepRequest
import konsyst.ru.features.steps.models.CreateStepResponse
import kotlinx.serialization.Serializable

@Serializable
data class StepsDataTransferObject(
    val id: Int? = null,
    val title: String,
    val description: String,
    val scenarioId: Int? = null,
    val action: String
)
fun CreateStepRequest.mapToStepDTO(): StepsDataTransferObject =
    StepsDataTransferObject(
        title = title,
        description = description,
        action = action
    )
fun StepsDataTransferObject.mapToCreateStepResponse(): CreateStepResponse =
    CreateStepResponse(
        id = id,
        title = title,
        description = description,
        scenarioId = scenarioId,
        action = action
    )