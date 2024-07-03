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

data class FetchHtmlResponse(
    val htmlMarkup: String
)


@Serializable
data class UserDataStepsResponse(
    val id: Int,
    val userId: Int,
    val eventId: Int,
    val scenarioId: Int,
    val stepId: Int,
    val videoFile: String?,
    val photoFiles: List<String>?,
    val userComment: String?,
    val videoData: ByteArray?,
    val photoData: List<ByteArray?>
)
