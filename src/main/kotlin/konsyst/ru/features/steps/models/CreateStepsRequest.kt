package konsyst.ru.features.steps.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateStepRequest(
    val title: String,
    val action: String,
    val number: Int
)

@Serializable
data class CreateStepResponse(
    val id: Int? = null,
    val title: String,
    val scenarioId: Int? = null,
    val action: String,
    val number: Int? = null
)