package konsyst.ru.features.events.models

import konsyst.ru.database.events.EventStatus
import kotlinx.serialization.Serializable


@Serializable
data class FetchEventsRequest (
    val searchQuery: String
)

data class FetchEventRequest(
    val id: Int
)


@Serializable
data class FetchEventsResponse(
    val events: List<EventResponse>
)

@Serializable
data class EventResponse(
    val id: Int? = null,
    val title: String,
    val date: String,
    val scenariosCount: Int? = null,
    val scenariosComplete: Int? = null,
    val userId: Int? = null,
    val status: EventStatus
)