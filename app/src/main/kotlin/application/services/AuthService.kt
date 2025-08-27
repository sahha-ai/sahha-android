package application.services

import features.auth.domain.AuthPort
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class AuthService(
    private val authPort: AuthPort,
    private val ioDispatcher: CoroutineContext  // Injected for coroutine context, e.g., Dispatchers.IO
) {
    /**
     * Registers a new profile and persists the token.
     * @return Result<Unit> indicating success or failure.
     */
    suspend fun register(appId: String, appSecret: String, externalId: String? = null): Result<Unit> {
        return withContext(ioDispatcher) {
            authPort.register(appId, appSecret, externalId)
        }
    }

    /**
     * Retrieves a valid (non-expired) token if available.
     * @return The token string or null if unavailable.
     */
    suspend fun getValidToken(): String? {
        return withContext(ioDispatcher) {
            authPort.getValidToken()
        }
    }

    /**
     * Clears all authentication state.
     */
    suspend fun clear() {
        withContext(ioDispatcher) {
            authPort.clear()
        }
    }
}