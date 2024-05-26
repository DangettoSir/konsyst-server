package konsyst.ru.database.steps

import konsyst.ru.features.steps.models.CreateStepRequest
import konsyst.ru.features.steps.models.CreateStepResponse
import konsyst.ru.features.steps.models.StepsResponse
import kotlinx.serialization.Serializable

@Serializable
data class StepsDataTransferObject(
    var id: Int? = null,
    val title: String,
    val scenarioId: Int? = null,
    val action: String,
    val number: Int? = null
)
fun CreateStepRequest.mapToStepDTO(): StepsDataTransferObject =
    StepsDataTransferObject(
        title = title,
        action = action,
        number = number
    )
fun StepsDataTransferObject.mapToCreateStepResponse(): CreateStepResponse =
    CreateStepResponse(
        id = id,
        title = title,
        scenarioId = scenarioId,
        action = action,
        number = number
    )
fun StepsDataTransferObject.mapToStepResponse(): StepsResponse =
    StepsResponse(
        id = id,
        title = title,
        scenarioId = scenarioId,
        action = action,
        number = number
    )
