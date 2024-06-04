package konsyst.ru.database.scenarios

import konsyst.ru.features.scenarios.models.CreateScenarioRequest
import konsyst.ru.features.scenarios.models.CreateScenarioResponse
import konsyst.ru.features.scenarios.models.ScenarioResponse
import kotlinx.serialization.Serializable

@Serializable
data class ScenariosDataTransferObject(
    var id: Int? = null,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val isCompleted: Boolean,
    val eventFrom: String? = null
)

fun CreateScenarioRequest.mapToScenarioDTO(): ScenariosDataTransferObject =
    ScenariosDataTransferObject(
        title = title,
        description = description,
        date = date,
        location = location,
        isCompleted = false,
    )

fun ScenariosDataTransferObject.mapToCreateScenarioResponse(): CreateScenarioResponse =
    CreateScenarioResponse(
        id = id,
        title = title,
        description = description,
        date = date,
        location = location,
        isCompleted = isCompleted,
    )

fun ScenariosDataTransferObject.mapToScenarioResponse(): ScenarioResponse =
    ScenarioResponse(
        id = id,
        title = title,
        description = description,
        date = date,
        location = location,
        isCompleted = isCompleted,
        eventFrom = eventFrom
    )
