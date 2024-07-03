package konsyst.ru.features.events.models

import kotlinx.serialization.Serializable


@Serializable
data class ReceiveUserAdd (
    val searchQuery: Int,
    val userId: Int
)