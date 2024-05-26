package konsyst.ru.utils
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*
import konsyst.ru.database.tokens.JwtConfig
import java.util.*

object JwtUtils {
    data class AuthenticationResult(
        val isAuthenticated: Boolean,
        val username: String?,
        val role: String?
    )
    fun generateJwtToken(config: JwtConfig, login: String, role: String? = null): String {
        val algorithm = Algorithm.HMAC256(config.secret)
        val issuer = config.issuer
        val audience = config.audience
        val expirationTime = Date(System.currentTimeMillis() + config.expirationTimeInMillis)

        val builder = JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("login", login)
            .withExpiresAt(expirationTime)

        if (role != null) {
            builder.withClaim("role", config.roles[role])
        }

        return builder.sign(algorithm)
    }

}