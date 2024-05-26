package konsyst.ru.database.users

data class UserDataTransferObject(
    val id: Int,
    val login: String,
    val hashedPassword: String,
    val username: String,
    val userNickname: String,
    val roleId: Int,
    var argon2ParamsId: Int
)

