package konsyst.ru.features.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginRecevie(
    val login: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val protectedToken: String,
    val username: String,
    val userNickname: String,
    val userId: Int
)