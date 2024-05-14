package konsyst.ru.features.tokens

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.tokens.Tokens
import konsyst.ru.database.tokens.mapToTokenResponse
import konsyst.ru.features.tokens.models.FetchTokensRequest
import konsyst.ru.features.tokens.models.FetchTokensResponse
import konsyst.ru.utils.TokenCheck

class TokensController {
    suspend fun Search(call: ApplicationCall) {
        val request = call.receive<FetchTokensRequest>()
        val token = call.request.headers["Bearer-Authorization"]
        if (TokenCheck.isTokenValid(token.orEmpty()) || TokenCheck.isTokenAdmin(token.orEmpty())) {
            call.respond(
                FetchTokensResponse(
                    tokens = Tokens.fetchTokens()
                        .filter { it.login.contains(request.searchQuery, ignoreCase = true) }
                        .map { it.mapToTokenResponse() }
                )
            )
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }
    }
}