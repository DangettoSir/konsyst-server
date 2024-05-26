package konsyst.ru.features.steps.models


import kotlinx.serialization.Serializable


@Serializable
data class FetchStepsRequest (
    val searchQuery: Int
)

@Serializable
data class FetchStepsResponse(
    val steps: List<StepsResponse>
)

@Serializable
data class StepsResponse(
    val id: Int? = null,
    val title: String,
    val scenarioId: Int? = null,
    val action: String,
    val number: Int? = null
)