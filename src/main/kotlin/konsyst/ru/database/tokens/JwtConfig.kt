package konsyst.ru.database.tokens

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*
import konsyst.ru.utils.JwtUtils
import java.util.*

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val secret: String,
    val expirationTimeInMillis: Long,
    val roles: Map<String, String>
) {

    fun createJwtVerifier(): JWTVerifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(login: String, role: String? = null): String = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("login", login)
        .withClaim("role", role)
        .withExpiresAt(Date(System.currentTimeMillis() + expirationTimeInMillis))
        .sign(Algorithm.HMAC256(secret))


    companion object{
        fun handleProtectedRequest(principal: JWTPrincipal?): JwtUtils.AuthenticationResult {
            val username = principal?.payload?.getClaim("username")?.asString()
            val login = principal?.payload?.getClaim("login")?.asString()
            val role = principal?.payload?.getClaim("role")?.asString()

            return JwtUtils.AuthenticationResult(
                isAuthenticated = (login != null && role != null),
                username = username,
                role = role
            )
        }
    }

}