package konsyst.ru.features.userdata.models

import io.ktor.http.content.*
import kotlinx.serialization.Serializable


data class CreateDataRequest(
    @Serializable
    val userId: Int,
    @Serializable
    val eventId: Int,
    @Serializable
    val scenarioId: Int,
    @Serializable
    val stepId: Int? = null,
    @Serializable(with = PartDataFileItemSerializer::class)
    val videoFile: PartData.FileItem?,
    @Serializable(with = PartDataFileItemSerializer::class)
    val photoFiles: List<PartData.FileItem>?,
    @Serializable
    val userComment: String?
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
