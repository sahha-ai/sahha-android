package sdk.sahha.android.common

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility

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
        val json = JSONObject(bodyString ?: "")
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

    internal suspend fun storeNewTokens(
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