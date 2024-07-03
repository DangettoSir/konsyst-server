package konsyst.ru.database.users

import kotlinx.serialization.Serializable
@Serializable
data class UserGroupsDTO(
    val id: Int,
    val groupName: String,
    val userCount: Int,
    val userIds: List<Int>
)
