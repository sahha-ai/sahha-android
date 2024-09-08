package sdk.sahha.android.common

import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.source.Sahha

private const val tag = "SahhaResponseHandler"

internal object SahhaResponseHandler {
    internal fun returnFormattedResponse(
        response: Response<ResponseBody>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        if (response.code() == 204) {
            callback?.also { it(null, "{}") }
            return
        }

        val reader = response.body()?.charStream()
        val bodyString = reader?.readText()
        val json = JSONArray(bodyString ?: "")
        val jsonString = json.toString(6)
        callback?.also { it(null, jsonString) }
    }

    internal suspend fun checkTokenExpired(
        code: Int,
        retryLogic: suspend () -> Unit
    ) {
        if (ResponseCode.isUnauthorized(code)) {
            Sahha.di.authRepo.postRefreshToken(retryLogic)
        }
    }

    internal suspend fun newTokenOnExpired(
        code: Int,
        retryLogic: suspend (newToken: String?) -> Unit,
    ) {
        if (ResponseCode.isUnauthorized(code)) {
            Sahha.di.authRepo.postRefreshTokenAndReturnNew(retryLogic)
        } else retryLogic(Sahha.di.authRepo.getToken())
    }

    internal fun storeNewTokens(
        tokenData: TokenData?,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        tokenData?.also {
            Sahha.di.authRepo.saveEncryptedTokens(
                it.profileToken,
                it.refreshToken
            ) { error, success ->
                callback?.invoke(error, success)
            }
        } ?: callback?.invoke(SahhaErrors.noToken, false)
    }
}