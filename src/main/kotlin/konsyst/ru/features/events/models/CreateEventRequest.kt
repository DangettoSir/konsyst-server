package konsyst.ru.features.events.models


import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
    val eventName: String,
    val eventTag: String,
    val eventTaD: String
)

@Serializable
data class CreateEventResponse(
    val eventId: String,
    val eventName: String,
    val eventTag: String,
    val eventTaD: String,
    val scenarioBundle: String
)