package konsyst.ru.features.userdata.models

import kotlinx.serialization.Serializable


@Serializable
data class FetchUserDataRequest(
    val id: Int
)

@Serializable
data class FetchUserDataStepsResponse(
    val dataSteps: List<UserDataStepsResponse>
)

@Serializable
data class UserDataStepsResponse(
    val id: Int?,
    val userId: Int?,
    val eventId: Int?,
    val scenarioId: Int?,
    val stepId: Int?,
    val videoFile: String = null.toString(),
    val photoFiles: List<String>? = null,
    val userComment: String? = null
)