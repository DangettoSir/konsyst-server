package konsyst.ru.features.userdata.models

import kotlinx.serialization.Serializable

@Serializable
data class GetDataSteps(
    val scenarioId: Int
)