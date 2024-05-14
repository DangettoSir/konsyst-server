package konsyst.ru.database.users

data class UserDataTransferObject(
    val login: String,
    val password: String,
    val username: String,
    var id: Int?
)

