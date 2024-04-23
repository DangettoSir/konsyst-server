package konsyst.ru.features.register

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import konsyst.ru.database.tokens.TokenDataTransferObject
import konsyst.ru.database.tokens.Tokens
import konsyst.ru.database.users.UserDataTransferObject
import konsyst.ru.database.users.Users
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.util.*

class RegisterController(val call: ApplicationCall) {

    suspend fun registerNewUser(){
        val registerReceive = call.receive<RegisterReceive>()

        val userDTO = Users.fetchUser(registerReceive.login)

        if (userDTO != null){
            call.respond(HttpStatusCode.Conflict, "User already exists")
        } else{
            val token = UUID.randomUUID().toString()
            try{
                Users.insert(
                    UserDataTransferObject(
                        login = registerReceive.login,
                        password = registerReceive.password,
                        username=""
                    )
                )
            } catch (e: ExposedSQLException){
                call.respond(HttpStatusCode.Conflict, "User already exists")
            } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Can't create user ${e.localizedMessage}")
            }

            Tokens.insert(TokenDataTransferObject(rowId = UUID.randomUUID().toString(), login = registerReceive.login, token = token))
            call.respond(RegisterResponse(token = token))
        }
    }
}