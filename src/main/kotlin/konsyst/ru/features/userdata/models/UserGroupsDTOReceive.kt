package konsyst.ru.features.userdata.models

import kotlinx.serialization.Serializable

@Serializable
data class UserGroupsDTOReceive(
    val groupName: String,
    val userIds: List<Int>
)