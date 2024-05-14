package konsyst.ru.features.events.models

import konsyst.ru.database.events.EventStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
    val title: String,
    val date: String,
)

@Serializable
data class CreateEventResponse(
    val id: Int? = null,
    val title: String,
    val date: String,
    val scenariosCount: Int? = null,
    val scenariosComplete: Int? = null,
    val userId: Int? = null,
    val status: EventStatus
)