package konsyst.ru.features.notifications.models

import konsyst.ru.database.events.EventStatus
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: Int,
    val title: String,
    val date: String,
    val scenariosCount: Int,
    val scenariosComplete: Int,
    val userId: Int,
    val status: EventStatus
)