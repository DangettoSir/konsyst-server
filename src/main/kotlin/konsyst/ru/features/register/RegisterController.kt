package konsyst.ru.features.register

import de.mkammerer.argon2.Argon2Factory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import konsyst.ru.database.tokens.TokenDataTransferObject
import konsyst.ru.database.tokens.Tokens
import konsyst.ru.database.users.UserDataTransferObject
import konsyst.ru.database.users.Users
import konsyst.ru.utils.TokenCheck
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.util.*
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.toCharArray
@ExperimentalEncodingApi
class RegisterController {

    suspend fun registerNewUser(call: ApplicationCall) {
        val registerReceive = call.receive<RegisterReceive>()
        val token = call.request.headers["Bearer-Authorization"]?.orEmpty()
        if (TokenCheck.isTokenAdmin(token.orEmpty())) {
            try {
                val (login, password) = generateCredentials()
                val hashedPassword = hashPassword(password, SALT)
                val userDTO = UserDataTransferObject(
                    id = generateUniqueId(),
                    login = login,
                    hashedPassword = hashedPassword,
                    username = registerReceive.username,
                    userNickname = registerReceive.userNickname,
                    roleId = registerReceive.roleId,
                    argon2ParamsId = 0
                )
                Users.insert(userDTO)
                Tokens.insert(
                    TokenDataTransferObject(
                        rowId = UUID.randomUUID().toString(),
                        login = login,
                        token = generateToken()
                    )
                )
                call.respond(RegisterResponse(login = login, password = password))
            } catch (e: ExposedSQLException) {
                call.respond(HttpStatusCode.Conflict, "User already exists")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Can't create user ${e.localizedMessage}")
            }
        }
        else {
            call.respond(HttpStatusCode.Unauthorized, "Token expired")
        }
    }


    private fun generateCredentials(): Pair<String, String> {
        val login = generateRandomString(8)
        val password = generateRandomString(8)
        return Pair(login, password)
    }

    private fun generateRandomString(length: Int): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(length) { chars.random() }.joinToString("")
    }

    private fun generateUniqueId(): Int {
        val random = Random()
        var uniqueId: Int
        val existingIds = Users.fetchUsers().map { it.id }.toSet()
        do {
            uniqueId = random.nextInt(Int.MAX_VALUE)
        } while (existingIds.contains(uniqueId))
        return uniqueId
    }

    private fun generateToken(): String {
        return UUID.randomUUID().toString()
    }
}
    private const val ARGON2_ITERATIONS = 32
    private const val ARGON2_PARALLELISM = 16
    private const val ARGON2_MEMORY_COST = 256 * 1024 // в байтах

    @ExperimentalEncodingApi
    private val SALT = "W9xY8dRxc2MzTmJmNWNlMDQwNjNlNjU3".decodeBase64Bytes()

    private fun hashPassword(password: String, SALT: ByteArray): String {
        val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
        val password = (password + String(SALT, Charsets.UTF_8)).toCharArray()
        return argon2.hash(ARGON2_ITERATIONS, ARGON2_MEMORY_COST, ARGON2_PARALLELISM, password)
    }
