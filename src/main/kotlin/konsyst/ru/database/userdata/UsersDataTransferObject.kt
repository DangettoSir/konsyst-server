package konsyst.ru.database.userdata

import io.ktor.http.content.*
import konsyst.ru.features.userdata.models.CreateDataRequest
import konsyst.ru.features.userdata.models.CreateUserDataStepsResponse
import kotlinx.serialization.Serializable
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Serializable
data class UsersDataTransferObject(
    var id: Int? = null,
    val userId: Int,
    val eventId: Int,
    val scenarioId: Int,
    val stepId: Int,
    val videoFilePath: String? = null,
    val photoFilePaths: List<String>? = null,
    val userComment: String? = null
)

fun CreateDataRequest.mapToUserDataDTO(): UsersDataTransferObject {
    val uploadsDirectory = "uploads"
    val userId = this.userId ?: 0
    val eventId = this.eventId ?: 0
    val scenarioId = this.scenarioId ?: 0
    val stepId = this.stepId ?: 0
    val userDirectory = "$uploadsDirectory/$userId"
    val eventDirectory = "$userDirectory/$eventId"
    val scenarioDirectory = "$eventDirectory/$scenarioId"
    val stepDirectory = "$scenarioDirectory/$stepId"

    val videoFilePath = if (videoFile != null) {
        val videoFileName = "$stepId-video.mp4"
        val videoFilePath = "$stepDirectory/videos/$videoFileName"
        saveFile(listOf(videoFile), videoFilePath).firstOrNull() ?: ""
    } else {
        ""
    }

    val photoFilePaths = mutableListOf<String>()
    photoFiles?.forEach { photoFile ->
        val photoFileName = "$stepId-photo-${photoFiles!!.indexOf(photoFile)}.jpg"
        val photoFilePath = "$stepDirectory/photos/$photoFileName"
        val savedPhotoFilePath = saveFile(listOf(photoFile), photoFilePath).firstOrNull() ?: ""
        if (savedPhotoFilePath.isNotEmpty()) {
            photoFilePaths.add(savedPhotoFilePath)
        }
    }

    return UsersDataTransferObject(
        id = 0,
        userId = userId,
        eventId = eventId,
        scenarioId = scenarioId,
        stepId = stepId,
        videoFilePath = videoFilePath,
        photoFilePaths = photoFilePaths.takeIf { it.isNotEmpty() },
        userComment = userComment ?: ""
    )
}

private fun saveFile(files: List<PartData.FileItem?>, directoryPath: String): List<String> {
    return saveFileInternal(files.filterNotNull().asSequence(), directoryPath)
}

private fun saveFile(file: PartData.FileItem?, directoryPath: String): String? {
    return saveFileInternal(sequenceOf(file).filterNotNull(), directoryPath).firstOrNull()
}
private fun saveFileInternal(files: Sequence<PartData.FileItem>, directoryPath: String): List<String> {
    if (files.none()) {
        return emptyList()
    }

    // Создание необходимых папок
    val directory = File(directoryPath)
    directory.mkdirs()

    // Сохранение файлов
    return files.mapIndexed { index, file ->
        val fileName = "$index-${file.originalFileName}"
        val filePath = "$directoryPath/$fileName"
        val outputFile = File(filePath)
        file.streamProvider().use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        filePath
    }.toList()
}


fun UsersDataTransferObject.mapToCreateUserDataResponse(): CreateUserDataStepsResponse =
    CreateUserDataStepsResponse(
        id = 0,
        userId = userId,
        eventId = eventId,
        scenarioId = scenarioId,
        stepId = stepId,
        videoFilePath = videoFilePath.orEmpty(),
        photoFilePaths = photoFilePaths.takeIf { it != null && it.isNotEmpty() },
        userComment = userComment.orEmpty()
    )

private fun saveFile(file: String?, fileType: String): String {
    val filePath = "uploads/$fileType/${file.hashCode()}"
    return filePath
}

private fun readFile(filePath: String): ByteArray {
    return Files.readAllBytes(Paths.get(filePath))
}
