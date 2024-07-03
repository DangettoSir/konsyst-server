package konsyst.ru.database.userdata

import kotlinx.serialization.Serializable

@Serializable
data class UsersDataTransferObject(
    var id: Int? = null,
    val userId: Int?,
    val eventId: Int?,
    val scenarioId: Int?,
    val stepId: Int?,
    val videoFilePath: String? = null,
    val photoFilePaths: List<String>? = null,
    val userComment: String? = null
)
