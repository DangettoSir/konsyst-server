package konsyst.ru.features.tokens

import io.ktor.server.application.*
import io.ktor.server.routing.*



fun Application.configureTokensRouting() {
    var tokensController = TokensController()
    routing {
        post("/tokens/search") {
            tokensController.Search(call)
        }
    }
}