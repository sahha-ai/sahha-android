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

object SahhaResponseHandler {
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

    internal suspend fun handleResponse(
        response: Response<*>,
        retryLogic: (suspend () -> Response<*>),
        successfulLogic: (suspend () -> Unit) = {},
        callback: (suspend (error: String?, successful: Boolean) -> Unit)? = null
    ) {
        if (ResponseCode.isUnauthorized(response.code())) {
            callback?.invoke(SahhaErrors.attemptingTokenRefresh, false)
            runBlocking {
                checkTokenExpired(response.code()) {
                    val retryResponse = retryLogic()
                    handleResponse(
                        retryResponse,
                        retryLogic,
                        successfulLogic,
                        callback
                    )
                }
            }
            return
        }

        if (ResponseCode.isSuccessful(response.code())) {
            runBlocking { successfulLogic() }
            callback?.also {
                it(null, true)
            }
            return
        }

        callback?.also {
            it(
                "${response.code()}: ${response.message()}",
                false
            )
        }

        Log.e(tag, "Error: ${response.code()}: ${response.message()}")
        Sahha.di.sahhaErrorLogger.api(response, SahhaErrors.typeRequest)
    }

    internal suspend fun handleResponse(
        call: Call<ResponseBody>,
        retryLogic: suspend (() -> Call<ResponseBody>),
        callback: ((error: String?, successful: Boolean) -> Unit)?,
        successfulLogic: (suspend () -> Unit) = {}
    ) {
        call.enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (ResponseCode.isUnauthorized(response.code())) {
                        callback?.also { it(SahhaErrors.attemptingTokenRefresh, false) }
                        runBlocking {
                            checkTokenExpired(response.code()) {
                                val retryResponse = retryLogic()
                                handleResponse(
                                    retryResponse,
                                    retryLogic,
                                    callback,
                                    successfulLogic
                                )
                            }
                        }
                        return
                    }

                    if (ResponseCode.isSuccessful(response.code())) {
                        runBlocking { successfulLogic() }
                        callback?.also {
                            it(null, true)
                        }
                        return
                    }

                    callback?.also {
                        it(
                            "${response.code()}: ${response.message()}",
                            false
                        )
                    }

                    Sahha.di.sahhaErrorLogger.api(call, response)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback?.also {
                        it(t.message, false)
                    }

                    Sahha.di.sahhaErrorLogger.api(
                        call,
                        SahhaErrors.typeResponse,
                        null,
                        t.message ?: SahhaErrors.responseFailure
                    )
                }
            }
        )
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