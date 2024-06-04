package konsyst.ru.features.login
import de.mkammerer.argon2.Argon2Factory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import konsyst.ru.database.tokens.JwtConfig
import konsyst.ru.database.tokens.Tokens
import konsyst.ru.database.users.Users
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.toCharArray
@ExperimentalEncodingApi
class LoginController(private val jwtConfig: JwtConfig) {
    @ExperimentalEncodingApi
    suspend fun loginExecute(call: ApplicationCall): Unit {
            val receive = try {
                call.receive<LoginRecevie>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
                return
            }
            val receivedLogin = receive.login
            val receivedPassword = receive.password
            val userDTO = Users.fetchUser(receivedLogin)
            val tokensDTO = Tokens.fetchToken(receivedLogin)
            if (userDTO == null) {
                call.respond(HttpStatusCode.Unauthorized, "User not found")
            } else {
                if (verifyPassword(receivedPassword, userDTO.hashedPassword)) {
                    val role = "user"
                    val token = tokensDTO?.token
                    val protectedToken = jwtConfig.generateToken(receive.login, role)
                    call.respond(
                        LoginResponse(
                            token = token.toString(),
                            protectedToken = protectedToken,
                            username = userDTO.username,
                            userNickname = userDTO.userNickname,
                            userId = userDTO.id
                        )
                    )
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid Password")
                }
            }
    }
}

private const val ARGON2_ITERATIONS = 32
private const val ARGON2_PARALLELISM = 16
private const val ARGON2_MEMORY_COST = 256 * 1024 // в байтах
@ExperimentalEncodingApi
private val SALT = "W9xY8dRxc2MzTmJmNWNlMDQwNjNlNjU3".decodeBase64Bytes()

@ExperimentalEncodingApi
private fun verifyPassword(receivedPassword: String, hashedPassword: String): Boolean {
    val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
    return argon2.verify(hashedPassword, (receivedPassword + String(SALT, Charsets.UTF_8)).toCharArray())
}
