package konsyst.ru.features.events.models

import kotlinx.serialization.Serializable


@Serializable
data class FetchEventStatusRequest (
    val searchQuery: Int
)



@Serializable
data class FetchEventStatusResponse(
    val status: Boolean
)
