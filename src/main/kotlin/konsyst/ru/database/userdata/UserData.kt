package konsyst.ru.database.userdata

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object UserDataSteps : Table("userdata_steps") {
    val id = integer("id")
    val userId = integer("user_id")
    val eventId = integer("event_id")
    val scenarioId = integer("scenario_id")
    val stepId = integer("step_id")
    val videoFilePath = varchar("video_file_path", 255)
    val photoFilePaths = text("photo_file_paths")
    val userComment = varchar("user_comment", 255)

    fun insert(userDataDTO: UsersDataTransferObject) {
        transaction {
            UserDataSteps.insert {
                it[id] = userDataDTO.id ?: 0
                it[userId] = userDataDTO.userId
                it[eventId] = userDataDTO.eventId
                it[scenarioId] = userDataDTO.scenarioId
                it[stepId] = userDataDTO.stepId
                it[videoFilePath] = userDataDTO.videoFilePath ?: ""
                it[photoFilePaths] = userDataDTO.photoFilePaths?.joinToString(", ") ?: ""
                it[userComment] = userDataDTO.userComment ?: ""
            }
        }
    }

    fun fetchData(id: Int): UsersDataTransferObject? {
        return try {
            transaction {
                UserDataSteps.select { UserDataSteps.id eq id }
                    .map { row ->
                        val photoFilePaths = row[UserDataSteps.photoFilePaths]?.split(", ")
                        UsersDataTransferObject(
                            id = row[UserDataSteps.id],
                            userId = row[UserDataSteps.userId],
                            eventId = row[UserDataSteps.eventId],
                            scenarioId = row[UserDataSteps.scenarioId],
                            stepId = row[UserDataSteps.stepId],
                            videoFilePath = row[UserDataSteps.videoFilePath],
                            photoFilePaths = listOf(row[UserDataSteps.photoFilePaths]),
                            userComment = row[UserDataSteps.userComment]
                        )
                    }
                    .singleOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchDatas(): List<UsersDataTransferObject> {
        return try {
            transaction {
                UserDataSteps.selectAll().map { row ->
                    val photoFilePaths = row[UserDataSteps.photoFilePaths]?.split(", ")
                    UsersDataTransferObject(
                        id = row[UserDataSteps.id],
                        userId = row[UserDataSteps.userId],
                        eventId = row[UserDataSteps.eventId],
                        scenarioId = row[UserDataSteps.scenarioId],
                        stepId = row[UserDataSteps.stepId],
                        videoFilePath = row[UserDataSteps.videoFilePath],
                        photoFilePaths = listOf(row[UserDataSteps.photoFilePaths]),
                        userComment = row[UserDataSteps.userComment]
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
