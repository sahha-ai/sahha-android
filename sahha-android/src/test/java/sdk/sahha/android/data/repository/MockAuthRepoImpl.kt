package sdk.sahha.android.data.repository

import retrofit2.Response
import retrofit2.mock.BehaviorDelegate
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.model.dto.send.ExternalIdSendDto
import sdk.sahha.android.domain.repository.AuthRepo

internal class MockAuthRepoImpl(
    private val delegate: BehaviorDelegate<AuthRepo> = AppModule.mockRetrofit.create(AuthRepo::class.java)
) : AuthRepo {
    private var token: String? = null
    private var refreshToken: String? = null

    override fun getToken(): String? {
        return token
    }

    override fun getRefreshToken(): String? {
        return refreshToken
    }

    override fun saveEncryptedTokens(
        profileToken: String,
        refreshToken: String,
        callback: (error: String?, success: Boolean) -> Unit
    ) {
        this.token = profileToken
        this.refreshToken = refreshToken

        if (profileToken.isEmpty() || refreshToken.isEmpty())
            callback("Tokens cannot be null", false)
        else
            callback(null, true)
    }

    override suspend fun postRefreshToken(
        retryLogic: suspend () -> Unit,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        retryLogic()
        callback?.invoke(null, true)
    }

    override suspend fun postRefreshTokenAndReturnNew(
        retryLogic: suspend (newToken: String?) -> Unit,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        retryLogic("new_refresh_token")
        callback?.invoke(null, true)
    }

    override suspend fun getTokensByExternalId(
        appId: String,
        appSecret: String,
        externalId: ExternalIdSendDto
    ): Response<TokenData> {
        val tokenData = TokenData(
            "mock_profile_token",
            "mock_refresh_token"
        )

        return delegate.returningResponse(tokenData).getTokensByExternalId(appId, appSecret, externalId)
    }

    override suspend fun clearTokenData(callback: suspend (error: String?, success: Boolean) -> Unit) {
        this.token = ""
        this.refreshToken = ""

        callback(null, true)
    }
}