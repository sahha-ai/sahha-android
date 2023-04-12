package sdk.sahha.android.domain.repository

import retrofit2.Response
import sdk.sahha.android.domain.model.dto.send.ExternalIdSendDto
import sdk.sahha.android.domain.model.auth.TokenData

interface AuthRepo {
    fun getToken(): String?
    fun getRefreshToken(): String?
    fun saveEncryptedTokens(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    )

    suspend fun postRefreshToken(
        retryLogic: (suspend () -> Unit),
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    )

    suspend fun getTokensByExternalId(
        appId: String,
        appSecret: String,
        externalId: ExternalIdSendDto
    ): Response<TokenData>
}