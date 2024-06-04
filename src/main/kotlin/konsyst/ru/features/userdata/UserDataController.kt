package konsyst.ru.features.userdata


import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.tokens.Tokens
import konsyst.ru.database.userdata.UserDataSteps
import konsyst.ru.database.userdata.mapToCreateUserDataResponse
import konsyst.ru.database.userdata.mapToUserDataDTO
import konsyst.ru.database.users.Users
import konsyst.ru.features.userdata.models.CreateDataRequest
import konsyst.ru.features.userdata.models.FetchUserDataStepsResponse
import konsyst.ru.features.userdata.models.UserDataStepsResponse
import konsyst.ru.utils.TokenCheck
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random


class UserDataController {
    suspend fun getDataSteps(call: ApplicationCall) {
        val token = call.request.headers["Bearer-Authorization"]?.orEmpty()
        if (!TokenCheck.isTokenValid(token.toString()) && !TokenCheck.isTokenAdmin(token.toString())) {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
            return
        }

        val login = fetchLoginFromTokenDatabase(token.toString())
        val userId = login?.let { fetchUserIdFromUserDatabase(it) }
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, "User not found")
            return
        }

        val dataSteps: List<UserDataStepsResponse> = transaction {
            UserDataSteps.select { UserDataSteps.userId eq userId }
                .map { row ->
                    UserDataStepsResponse(
                        id = row[UserDataSteps.id],
                        userId = row[UserDataSteps.userId],
                        eventId = row[UserDataSteps.eventId],
                        scenarioId = row[UserDataSteps.scenarioId],
                        stepId = row[UserDataSteps.stepId],
                        videoFile = row[UserDataSteps.videoFilePath],
                        photoFiles = listOf(row[UserDataSteps.photoFilePaths]),
                        userComment = row[UserDataSteps.userComment]
                    )
                }
        }

        call.respond(FetchUserDataStepsResponse(dataSteps = dataSteps))
    }

    suspend fun createUserDataStep(call: ApplicationCall) {
        val token = call.request.headers["Bearer-Authorization"]
        if (!TokenCheck.isTokenValid(token.toString()) && !TokenCheck.isTokenAdmin(token.toString())) {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
            return
        }

        val logger: Logger = LoggerFactory.getLogger(this::class.java)
        logger.info("Received CreateDataRequest: $call")

        val request = call.receiveMultipart()
        val createDataRequest = CreateDataRequest()

        request.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "userId" -> {
                            if (part.value.isNotEmpty()) {
                                createDataRequest.userId = part.value.toInt()
                            } else {
                                createDataRequest.userId = null
                                logger.warn("Received empty userId, setting to null")
                            }
                        }
                        "eventId" -> {
                            if (part.value.isNotEmpty()) {
                                createDataRequest.eventId = part.value.toInt()
                            } else {
                                createDataRequest.eventId = null
                                logger.warn("Received empty eventId, setting to null")
                            }
                        }
                        "scenarioId" -> {
                            if (part.value.isNotEmpty()) {
                                createDataRequest.scenarioId = part.value.toInt()
                            } else {
                                createDataRequest.scenarioId = null
                                logger.warn("Received empty scenarioId, setting to null")
                            }
                        }
                        "stepId" -> {
                            if (part.value.isNotEmpty()) {
                                createDataRequest.stepId = part.value.toInt()
                            } else {
                                createDataRequest.stepId = null
                                logger.warn("Received empty stepId, setting to null")
                            }
                        }
                        "userComment" -> createDataRequest.userComment = part.value
                    }
                }
                is PartData.FileItem -> {
                    when (part.name) {
                        "videoFile" -> createDataRequest.videoFile = part
                        "photoFiles" -> createDataRequest.photoFiles = (createDataRequest.photoFiles ?: mutableListOf()) + part
                    }
                }
                else -> {}
            }
        }

        logger.info("Received CreateDataRequest: $createDataRequest")

        val userDataStep = createDataRequest.mapToUserDataDTO()
        logger.info("Mapped CreateDataRequest to UsersDataTransferObject: $userDataStep")

        val existingIds = UserDataSteps.fetchDatas().map { it.id }.toSet()
        logger.info("Fetched existing IDs: $existingIds")

        userDataStep.id = generateUniqueId(existingIds)
        logger.info("Generated unique ID: ${userDataStep.id}")

        logger.info("Inserting UsersDataTransferObject into database")
        try {
            UserDataSteps.insert(userDataStep)
        } catch (e: Exception) {
            logger.error("Error inserting UsersDataTransferObject into database: $e")
            call.respond(HttpStatusCode.InternalServerError, "Error creating user data step")
            return
        }

        val response = userDataStep.mapToCreateUserDataResponse()
        logger.info("Responding with CreateUserDataStepsResponse: $response")
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