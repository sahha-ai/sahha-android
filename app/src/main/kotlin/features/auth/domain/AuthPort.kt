package features.auth.domain

interface AuthPort {
    /** Registers/creates a profile on the server and persists the token. */
    suspend fun register(appId: String, appSecret: String, externalId: String? = null): Result<Unit>


    /** Returns a valid (non-expired) token string (without the "Bearer ") or null if unavailable. */
    suspend fun getValidToken(): String?


    /** Clears all auth state (tokens, metadata). */
    suspend fun clear()
}