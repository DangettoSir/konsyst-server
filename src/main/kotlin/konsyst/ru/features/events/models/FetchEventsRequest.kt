package konsyst.ru.features.events.models

import kotlinx.serialization.Serializable


@Serializable
data class FetchEventsRequest (
    val searchQuery: String
)

@Serializable
data class FetchEventsResponse(
    val events: List<EventResponse>
)

@Serializable
data class EventResponse(
    val eventId: String,
    val eventName: String,
    val eventTag: String,
    val eventTaD: String,
    val scenarioBundle: String,
)