package konsyst.ru.database.tokens

import konsyst.ru.features.tokens.models.TokenResponse
import kotlinx.serialization.Serializable

@Serializable
data class TokenDataTransferObject (
    val rowId: String,
    val login: String,
    val token: String
)

fun TokenDataTransferObject.mapToTokenResponse(): TokenResponse =
    TokenResponse(
        rowId = rowId,
        login = login,
        token = token
    )