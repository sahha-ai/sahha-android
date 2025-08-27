package features.auth.infra

import features.auth.model.AuthToken
import features.storage.domain.StorageKey
import features.storage.domain.StoragePort

/** Keys for persisted auth state in SecureStore. */
internal object AuthKeys {
    val TOKEN = StorageKey("auth_token", AuthToken.serializer())
}

/** Abstraction over secure persistence for tokens. */
internal class AuthStore(
    private val secureStore: StoragePort
) {
    suspend fun save(token: AuthToken) = secureStore.put(AuthKeys.TOKEN, token)
    suspend fun load(): AuthToken? = secureStore.get(AuthKeys.TOKEN)
    suspend fun clear() = secureStore.remove(AuthKeys.TOKEN)
}