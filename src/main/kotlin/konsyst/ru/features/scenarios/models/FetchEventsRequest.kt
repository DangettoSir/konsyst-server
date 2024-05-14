package konsyst.ru.features.scenarios.models

import konsyst.ru.database.events.EventStatus
import kotlinx.serialization.Serializable


@Serializable
data class FetchScenariosRequest (
    val searchQuery: Int
)

@Serializable
data class FetchScenariosResponse(
    val scenarios: List<ScenarioResponse>
)

@Serializable
data class ScenarioResponse(
    val id: Int? = null,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val isCompleted: Boolean
)