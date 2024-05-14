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
import kotlin.random.Random

class RegisterController {

    suspend fun registerNewUser(call: ApplicationCall) {
        val registerReceive = call.receive<RegisterReceive>()
        val userDTO = Users.fetchUser(registerReceive.login)

        if (userDTO != null) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
        } else {
            val token = UUID.randomUUID().toString()
            val userDTO = registerReceive.mapToUserDTO()

            val existingIds = Users.fetchUsers().map { it.id }.toSet()
            userDTO.id = generateUniqueId(existingIds)

            try {
                Users.insert(userDTO)
                Tokens.insert(TokenDataTransferObject(rowId = UUID.randomUUID().toString(), login = registerReceive.login, token = token))
                call.respond(RegisterResponse(token = token))
            } catch (e: ExposedSQLException) {
                call.respond(HttpStatusCode.Conflict, "User already exists")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Can't create user ${e.localizedMessage}")
            }
        }
    }

    fun generateUniqueId(existingIds: Set<Int?>): Int {
        var uniqueId: Int
        do {
            uniqueId = Random.nextInt(Int.MAX_VALUE)
        } while (existingIds.contains(uniqueId))
        return uniqueId
    }

    private fun RegisterReceive.mapToUserDTO(): UserDataTransferObject {
        return UserDataTransferObject(
            login = this.login,
            password = this.password,
            username = "",
            id = null
        )
    }
}
