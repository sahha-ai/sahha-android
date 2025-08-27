package features.auth.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
internal data class AuthToken(
    val value: String,
    val tokenType: TokenType = TokenType.Profile,
    val expiresAtEpochSeconds: Long // system UTC seconds
) {
    fun isValid(now: Instant = Instant.now()): Boolean = now.epochSecond < expiresAtEpochSeconds - CLOCK_SKEW

    companion object {
        private const val CLOCK_SKEW = 30 // seconds

        /** Prefer decoding JWT `exp`. If not a JWT/exp missing, use `expiresIn` relative to now. */
        fun fromResponse(resp: AuthResponse, now: Instant = Instant.now()): AuthToken {
            val token = resp.profileToken
            val expFromJwt = Jwt.decodeExpSecondsOrNull(token)
            val exp = expFromJwt ?: (now.epochSecond + (resp.expiresIn ?: 3600))
            return AuthToken(
                value = token,
                tokenType = runCatching { TokenType.valueOf(resp.tokenType) }.getOrDefault(TokenType.Unknown),
                expiresAtEpochSeconds = exp
            )
        }
    }
}