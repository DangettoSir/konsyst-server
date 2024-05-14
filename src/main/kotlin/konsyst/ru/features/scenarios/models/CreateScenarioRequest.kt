package konsyst.ru.features.scenarios.models


import kotlinx.serialization.Serializable

@Serializable
data class CreateScenarioRequest(
    val title: String,
    val description: String,
    val date: String,
    val location: String
)

@Serializable
data class CreateScenarioResponse(
    val id: Int? = null,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val isCompleted: Boolean
)