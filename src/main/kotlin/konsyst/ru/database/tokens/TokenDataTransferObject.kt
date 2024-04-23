package konsyst.ru.database.tokens

data class TokenDataTransferObject (
    val rowId: String,
    val login: String,
    val token: String
)