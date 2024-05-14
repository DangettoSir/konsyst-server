package konsyst.ru.features.login
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.tokens.TokenDataTransferObject
import konsyst.ru.database.tokens.Tokens
import konsyst.ru.database.users.Users
import java.util.*

class LoginController {
    suspend fun loginExecute(call: ApplicationCall) {
        val receive = try {
            call.receive<LoginRecevie>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
            return
        }

        val userDTO = Users.fetchUser(receive.login)
        if (userDTO == null) {
            call.respond(HttpStatusCode.Unauthorized, "User not found")
        } else {
            if (userDTO.password == receive.password) {
                val token = UUID.randomUUID().toString()
                Tokens.insert(
                    TokenDataTransferObject(
                        rowId = UUID.randomUUID().toString(),
                        login = receive.login,
                        token = token
                    )
                )
                call.respond(LoginResponse(token = token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid Password")
            }
        }
    }
}
