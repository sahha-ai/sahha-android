package features.auth.model

/**
 * Server response for profile authentication.
 *
 * {
 *   "profileToken": "<JWT>",
 *   "expiresIn": 86400,
 *   "tokenType": "Profile",
 *   "refreshToken": "<opaque or JWT>"
 * }
 */
internal data class AuthResponse(
    val profileToken: String,
    val expiresIn: Long? = null,   // we will not rely on this for expiry
    val tokenType: String,
    val refreshToken: String? = null
)
