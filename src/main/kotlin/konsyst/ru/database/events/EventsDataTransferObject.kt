package konsyst.ru.database.events

import kotlinx.serialization.Serializable
import konsyst.ru.features.events.models.CreateEventRequest
import konsyst.ru.features.events.models.CreateEventResponse
import konsyst.ru.features.events.models.EventResponse

@Serializable
data class EventDataTransferObject(
    var id: Int? = null,
    val title: String,
    val date: String,
    val scenariosCount: Int? = null,
    val scenariosComplete: Int? = null,
    val userId: Int? = null,
    val status: EventStatus
)

fun CreateEventRequest.mapToEventDTO(): EventDataTransferObject =
    EventDataTransferObject(
        title = title,
        date = date,
        status = EventStatus.UPCOMING
    )

fun EventDataTransferObject.mapToCreateEventResponse(): CreateEventResponse =
    CreateEventResponse(
        id = id,
        title = title,
        date = date,
        scenariosCount = scenariosCount,
        scenariosComplete = scenariosComplete,
        userId = userId,
        status = status
    )

fun EventDataTransferObject.mapToEventResponse(): EventResponse =
    EventResponse(
        id = id,
        title = title,
        date = date,
        scenariosCount = scenariosCount,
        scenariosComplete = scenariosComplete,
        userId = userId,
        status = status
    )
