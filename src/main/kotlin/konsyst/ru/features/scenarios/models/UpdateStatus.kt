package konsyst.ru.features.scenarios.models

import kotlinx.serialization.Serializable


@Serializable
data class UpdateStatus (
    val id: Int,
    val eventId: Int,
    val isCompleted: Boolean
)

@Serializable
data class Response(
    val id: Int,
    val eventId: Int,
    val isCompleted: Boolean
)