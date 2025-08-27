package features.auth.infra

import features.auth.domain.AuthPort
import features.auth.model.AuthToken
import java.time.Instant

internal class AuthAdapter(
    private val api: AuthApi,
    private val store: AuthStore,
    private val now: () -> Instant = { Instant.now() }
) : AuthPort {

    @Volatile
    private var inMemory: AuthToken? = null

    override suspend fun register(
        appId: String,
        appSecret: String,
        externalId: String?
    ): Result<Unit> = runCatching {
        val resp = api.register(appId, appSecret, RegisterBody(externalId))
        val token = AuthToken.fromResponse(resp, now())
        store.save(token)
        inMemory = token
    }


    override suspend fun getValidToken(): String? {
        val mem = inMemory
        if (mem != null && mem.isValid(now())) return mem.value


        val persisted = store.load()
        return if (persisted != null && persisted.isValid(now())) {
            inMemory = persisted
            persisted.value
        } else null
    }


    override suspend fun clear() {
        inMemory = null
        store.clear()
    }
}