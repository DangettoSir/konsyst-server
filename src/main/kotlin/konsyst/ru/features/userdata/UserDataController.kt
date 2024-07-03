package konsyst.ru.features.userdata


import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.tokens.Tokens
import konsyst.ru.database.userdata.UserDataSteps
import konsyst.ru.database.userdata.UsersDataTransferObject
import konsyst.ru.database.users.UserGroups
import konsyst.ru.database.users.UserGroupsDTO
import konsyst.ru.database.users.Users
import konsyst.ru.database.users.Users.fetchUsersCount
import konsyst.ru.database.users.insert
import konsyst.ru.features.userdata.models.*
import konsyst.ru.utils.TokenCheck
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.random.Random


class UserDataController {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    suspend fun uploadFile(call: ApplicationCall) {
        val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)
        logger.debug("Receiving multipart data")
        val multipartData = call.receiveMultipart()
        var userId: Int? = null
        var eventId: Int? = null
        var scenarioId: Int? = null
        var stepId: Int? = null
        var videoFile: PartData.FileItem? = null
        val photoFiles: MutableList<PartData.FileItem> = mutableListOf()
        var userComment: String? = null
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "userId" -> {
                            userId = part.value.toIntOrNull()
                            logger.debug("Received userId: $userId")
                        }
                        "eventId" -> {
                            eventId = part.value.toIntOrNull()
                            logger.debug("Received eventId: $eventId")
                        }
                        "scenarioId" -> {
                            scenarioId = part.value.toIntOrNull()
                            logger.debug("Received scenarioId: $scenarioId")
                        }
                        "stepId" -> {
                            stepId = part.value.toIntOrNull()
                            logger.debug("Received stepId: $stepId")
                        }
                        "userComment" -> {
                            userComment = part.value
                            logger.debug("Received userComment: $userComment")
                        }
                    }
                }
                is PartData.FileItem -> {
                    when (part.name) {
                        "videoFile" -> {
                            videoFile = part
                            logger.debug("Received videoFile")
                        }
                        "photoFiles[]" -> {
                            photoFiles.add(part)
                            logger.debug("Received photoFile")
                        }
                    }
                }

                else -> {}
            }
            true
        }

        val createDataRequest = CreateDataRequest(
            userId = userId ?: throw IllegalArgumentException("userId is required"),
            eventId = eventId ?: throw IllegalArgumentException("eventId is required"),
            scenarioId = scenarioId ?: throw IllegalArgumentException("scenarioId is required"),
            stepId = stepId ?: 0,
            videoFile = videoFile,
            photoFiles = if (photoFiles.isNotEmpty()) photoFiles else null,
            userComment = userComment
        )

        logger.debug("Saving files to uploads folder")
        val videoFilePath = saveFile(createDataRequest.videoFile, userId, eventId, scenarioId, stepId, "videos")
        val photoFilePaths = createDataRequest.photoFiles?.mapNotNull { file ->
            val filePath = saveFile(file, userId, eventId, scenarioId, stepId, "photos")
            filePath
        }?.joinToString(",")
        val existingIds = UserDataSteps.fetchUserDatas().map { it.id }.toSet()
        logger.debug("Saving data to the database")
        val userDataStepsDTO = UsersDataTransferObject(
            id = generateUniqueId(existingIds),
            userId = createDataRequest.userId.toInt(),
            eventId = createDataRequest.eventId.toInt(),
            scenarioId = createDataRequest.scenarioId.toInt(),
            stepId = createDataRequest.stepId?.toInt(),
            videoFilePath = videoFilePath,
            photoFilePaths = photoFilePaths?.split(","),
            userComment = createDataRequest.userComment
        )
        UserDataSteps.insert(userDataStepsDTO)

        logger.info("File uploaded successfully")

        call.respond(HttpStatusCode.OK, "File uploaded successfully")
    }


    private fun saveFile(
        file: PartData.FileItem?,
        userId: Int?,
        eventId: Int?,
        scenarioId: Int?,
        stepId: Int?,
        folderName: String
    ): String? {
        if (file == null) return null

        val folderPath = "uploads${File.separator}$userId${File.separator}$eventId${File.separator}$scenarioId${File.separator}$stepId${File.separator}$folderName"
        val filePath = "$folderPath${File.separator}${file.originalFileName}"

        val uploadDirectory = File(folderPath)
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs()
        }

        val targetFile = File(filePath)
        file.streamProvider().use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return filePath
    }

    suspend fun getDataSteps(call: ApplicationCall) {
        val receive = call.receive<GetDataSteps>()
        val token = call.request.headers["Bearer-Authorization"]?.orEmpty()
        if (!TokenCheck.isTokenAdmin(token.orEmpty())) {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
            return
        }
        val scenarioId = receive.scenarioId
        val dataSteps: List<UserDataStepsResponse> = transaction {
            UserDataSteps.select { UserDataSteps.scenarioId eq scenarioId }
                .map { row ->
                    val photoFilePaths = row[UserDataSteps.photoFilePaths]
                    val photoData = if (photoFilePaths != null) {
                        photoFilePaths.split(",")
                            .map { it.trim() }
                            .map { it.replace("\\", "/") } // Заменяем все обратные слэши на прямые
                            .filterNot { it.isBlank() }
                            .mapNotNull { getPhotoData(it) }
                    } else {
                        emptyList()
                    }

                    val videoFilePath = row[UserDataSteps.videoFilePath]
                    val videoData = if (videoFilePath != null) {
                        getVideoData(videoFilePath)
                    } else {
                        null
                    }

                    UserDataStepsResponse(
                        id = row[UserDataSteps.id],
                        userId = row[UserDataSteps.userId],
                        eventId = row[UserDataSteps.eventId],
                        scenarioId = row[UserDataSteps.scenarioId],
                        stepId = row[UserDataSteps.stepId],
                        videoFile = videoFilePath,
                        photoFiles = photoFilePaths?.split(",")?.map { it.trim() },
                        userComment = row[UserDataSteps.userComment],
                        videoData = videoData,
                        photoData = photoData
                    )
                }
        }
        
        call.respond(FetchUserDataStepsResponse(dataSteps = dataSteps))
    }
    private fun getVideoData(videoFilePath: String): ByteArray? {
        return try {
            // Читаем содержимое видео файла в ByteArray
            Files.readAllBytes(Paths.get(videoFilePath))
        } catch (e: Exception) {
            // Логируем ошибку
            logger.error("Error getting video data: $e")
            null
        }
    }

    suspend fun GetUserGroups(call: ApplicationCall) {
        val token = call.request.headers["Bearer-Authorization"] ?: return call.respond(HttpStatusCode.Unauthorized, "Token is missing")
        if (!TokenCheck.isTokenAdmin(token)) {
            return call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }

        val groups = transaction {
            UserGroups.selectAll()
                .map { userGroup ->
                    val userIds = userGroup[UserGroups.userIds]
                        ?.split(",")
                        ?.mapNotNull { it.toIntOrNull() }
                        ?.toList() ?: emptyList()

                    UserGroupsDTO(
                        id = userGroup[UserGroups.id],
                        groupName = userGroup[UserGroups.groupName],
                        userCount = userGroup[UserGroups.userCount],
                        userIds = userIds
                    )
                }
        }

        call.respond(groups)
    }
    suspend fun createUserGroup(call: ApplicationCall) {
        val token = call.request.headers["Bearer-Authorization"] ?: return call.respond(HttpStatusCode.Unauthorized, "Token is missing")
        if (!TokenCheck.isTokenAdmin(token)) {
            return call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }

        val userGroupDTO = try {
            call.receive<UserGroupsDTOReceive>()
        } catch (e: Exception) {
            return call.respond(HttpStatusCode.BadRequest, "Invalid request data: ${e.message}")
        }

        val newGroup = UserGroupsDTO(
            id = 0, // id будет установлен автоматически
            groupName = userGroupDTO.groupName,
            userCount = userGroupDTO.userIds.size,
            userIds = userGroupDTO.userIds
        )

        insert(newGroup)

        call.respond(HttpStatusCode.Created, newGroup)
    }


    private fun getPhotoData(photoFilesPaths: String): ByteArray? {
        return try {
            // Читаем содержимое фото файла в ByteArray
            Files.readAllBytes(Paths.get(photoFilesPaths))
        } catch (e: Exception) {
            // Логируем ошибку
            logger.error("Error getting photo data: $e")
            null
        }
    }


    suspend fun GetUsersCount(call: ApplicationCall){
        val token = call.request.headers["Bearer-Authorization"]
        if (!TokenCheck.isTokenValid(token.toString()) && !TokenCheck.isTokenAdmin(token.toString())) {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
            return
        }
        val response = fetchUsersCount()
        call.respond(response)
    }



    private fun fetchLoginFromTokenDatabase(token: String): String? {
        return transaction {
            Tokens.select { Tokens.token eq token }
                .mapNotNull { it[Tokens.login] }
                .singleOrNull()
        }
    }

    private fun fetchUserIdFromUserDatabase(login: String): Int? {
        return transaction {
            Users.select { Users.login eq login }
                .map { it[Users.id] }
                .singleOrNull()
        }
    }
    fun generateUniqueId(existingIds: Set<Int?>): Int {
        var uniqueId: Int
        do {
            uniqueId = Random.nextInt(Int.MAX_VALUE)
        } while (existingIds.contains(uniqueId))
        return uniqueId
    }
}