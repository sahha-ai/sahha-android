package sdk.sahha.android.data.repository

import android.content.SharedPreferences
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaResponseHandler.storeNewTokens
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.data.Constants.UERT
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.model.dto.send.ExternalIdSendDto
import sdk.sahha.android.domain.model.dto.send.RefreshTokenSendDto
import sdk.sahha.android.domain.repository.AuthRepo

private const val tag = "AuthRepoImpl"

class AuthRepoImpl(
    private val api: SahhaApi,
    private val encryptedSharedPreferences: SharedPreferences,
) : AuthRepo {
    override fun getToken(): String? {
        return encryptedSharedPreferences.getString(UET, null)
    }

    override fun getRefreshToken(): String? {
        return encryptedSharedPreferences.getString(UERT, null)
    }

    override suspend fun clearTokenData(callback: suspend (error: String?, success: Boolean) -> Unit) {
        try {
            val successful = encryptedSharedPreferences.edit().let {
                it.remove(UET)
                it.remove(UERT)
                it.commit()
            }
            callback(null, successful)
        } catch (e: Exception) {
            callback(e.message ?: SahhaErrors.somethingWentWrong, false)
        }
    }

    override suspend fun postRefreshToken(
        retryLogic: (suspend () -> Unit),
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        val tokenData = getTokenData(callback) ?: return
        try {
            val response = api.postRefreshTokenResponse(
                TokenBearer(tokenData.profileToken),
                RefreshTokenSendDto(tokenData.refreshToken)
            )

            if (ResponseCode.isSuccessful(response.code())) {
                handleSuccessfulResponse(response, retryLogic, callback)
            } else {
                handleFailedResponse(response, callback)
            }
        } catch (e: Exception) {
            callback?.invoke(e.message, false)
        }
    }

    override suspend fun postRefreshTokenAndReturnNew(
        retryLogic: (suspend (newToken: String?) -> Unit),
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        val tokenData = getTokenData(callback) ?: return
        try {
            val response = api.postRefreshTokenResponse(
                TokenBearer(tokenData.profileToken),
                RefreshTokenSendDto(tokenData.refreshToken)
            )

            if (ResponseCode.isSuccessful(response.code())) {
                handleSuccessfulResponse(response, retryLogic, callback)
            } else {
                handleFailedResponse(response, callback)
            }
        } catch (e: Exception) {
            callback?.invoke(e.message, false)
        }
    }

    private fun getTokenData(callback: ((error: String?, successful: Boolean) -> Unit)?): TokenData? {
        val token = getToken()
        val refreshToken = getRefreshToken()

        if (token == null || refreshToken == null) {
            callback?.invoke(SahhaErrors.noToken, false)
            return null
        }

        return TokenData(token, refreshToken)
    }

    private suspend fun handleSuccessfulResponse(
        response: Response<TokenData>,
        retryLogic: (suspend () -> Unit),
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        storeNewTokens(response.body(), callback)
        retryLogic()
    }

    private suspend fun handleSuccessfulResponse(
        response: Response<TokenData>,
        retryLogic: (suspend (newToken: String?) -> Unit),
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        val tokens = response.body()
        storeNewTokens(tokens, callback)
        retryLogic(tokens?.profileToken)
    }

    private fun handleFailedResponse(
        response: Response<TokenData>,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        callback?.invoke(response.message(), false)
    }


    override fun saveEncryptedTokens(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        try {
            val successful = encryptedSharedPreferences.edit().let {
                it.putString(UET, profileToken)
                it.putString(UERT, refreshToken)
                it.commit()
            }
            callback(null, successful)
        } catch (e: Exception) {
            callback(e.message ?: SahhaErrors.noToken, false)
        }
    }

    override suspend fun getTokensByExternalId(
        appId: String,
        appSecret: String,
        externalId: ExternalIdSendDto
    ): Response<TokenData> {
        return api.postExternalIdForToken(
            appId,
            appSecret,
            externalId
        )
    }
}