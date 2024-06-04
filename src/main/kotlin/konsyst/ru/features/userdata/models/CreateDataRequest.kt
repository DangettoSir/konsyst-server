package konsyst.ru.features.userdata.models

import io.ktor.http.content.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


data class CreateDataRequest(
    @Serializable
    var userId: Int? = null,
    @Serializable
    var eventId: Int? = null,
    @Serializable
    var scenarioId: Int? = null,
    @Serializable
    var stepId: Int? = null,
    @Contextual
    var videoFile: PartData.FileItem? = null,
    @Contextual
    var photoFiles: List<PartData.FileItem>? = null,
    @Serializable
    var userComment: String? = null
)

@Serializable
data class CreateUserDataStepsResponse(
    val id: Int?,
    val userId: Int?,
    val eventId: Int?,
    val scenarioId: Int?,
    val stepId: Int?,
    val videoFilePath: String? = null,
    val photoFilePaths: List<String>? = null,
    val userComment: String? = null
)
