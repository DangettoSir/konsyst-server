package konsyst.ru.features.login

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.tokens.TokenDataTransferObject
import konsyst.ru.database.tokens.Tokens
import konsyst.ru.database.users.Users
import java.util.*

class LoginController(private val call: ApplicationCall) {
    suspend fun loginExecute(){
        val receive = call.receive<LoginRecevie>()
        val userDTO = Users.fetchUser(receive.login)
        if(userDTO == null){
            call.respond(HttpStatusCode.BadRequest, "User not found")
        } else {
            if (userDTO.password == receive.password) {
                val token = UUID.randomUUID().toString()
                Tokens.insert(TokenDataTransferObject(rowId = UUID.randomUUID().toString(), login = receive.login, token = token))
                call.respond(LoginResponse(token = token))
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid Password")
            }
        }
    }
}