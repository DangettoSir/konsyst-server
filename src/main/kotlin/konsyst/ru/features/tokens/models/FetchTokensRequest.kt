package konsyst.ru.features.tokens.models

import kotlinx.serialization.Serializable

@Serializable
data class FetchTokensRequest (
    var searchQuery: String
)

@Serializable
data class FetchTokensResponse(
    val tokens: List<TokenResponse>
)

@Serializable
data class TokenResponse(
    val rowId: String,
    val login: String,
    val token: String
)