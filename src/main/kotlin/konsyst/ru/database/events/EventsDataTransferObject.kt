package konsyst.ru.database.events

import java.util.UUID
import konsyst.ru.features.events.models.CreateEventRequest
import konsyst.ru.features.events.models.CreateEventResponse
import konsyst.ru.features.events.models.EventResponse

data class EventsDataTransferObject(
    val eventId: String,
    val eventName: String,
    val eventTag: String,
    val eventTaD: String,
    val scenarioBundle: String
)

fun CreateEventRequest.mapToEventDTO(): EventsDataTransferObject =
    EventsDataTransferObject(
        eventId = UUID.randomUUID().toString(),
        eventName = eventName,
        eventTag = eventTag,
        eventTaD = eventTaD,
        scenarioBundle = UUID.randomUUID().toString()
    )

fun EventsDataTransferObject.mapToCreateEventResponse(): CreateEventResponse =
    CreateEventResponse(
        eventId = eventId,
        eventName = eventName,
        eventTag = eventTag,
        eventTaD = eventTaD,
        scenarioBundle = scenarioBundle
    )

fun EventsDataTransferObject.mapToEventResponse(): EventResponse = EventResponse(
    eventId = eventId,
    eventName = eventName,
    eventTag = eventTag,
    eventTaD = eventTaD,
    scenarioBundle = scenarioBundle
)