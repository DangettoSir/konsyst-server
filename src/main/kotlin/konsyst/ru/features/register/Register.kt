package konsyst.ru.features.register

import kotlinx.serialization.Serializable


@Serializable
data class RegisterReceive(
    val username: String,
    val userNickname : String,
    val roleId : Int
)


@Serializable
data class RegisterResponse(
    val login: String,
    val password: String
)
