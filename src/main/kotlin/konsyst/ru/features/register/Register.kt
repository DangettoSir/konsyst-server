package konsyst.ru.features.register

import kotlinx.serialization.Serializable


@Serializable
data class RegisterReceive(
    val login: String,
    val password: String
)


@Serializable
data class RegisterResponse(
    val token: String
)
