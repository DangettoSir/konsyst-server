package konsyst.ru.features.scenarios.models

import kotlinx.serialization.Serializable


@Serializable
data class LinkStepsRequest(
    val scenarioId: Int,
    val stepIds: List<Int>
)