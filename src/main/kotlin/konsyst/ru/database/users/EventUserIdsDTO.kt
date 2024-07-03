package konsyst.ru.database.users

import kotlinx.serialization.Serializable

@Serializable
data class EventUserIdsDTO(
    val id: Int,
    val event_id: Int,
    val user_id: Int
)
