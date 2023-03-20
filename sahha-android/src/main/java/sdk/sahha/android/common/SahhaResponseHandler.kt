package sdk.sahha.android.common

import kotlinx.coroutines.launch
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
            postRefreshToken(retryLogic)
        }
    }

    internal fun getRefreshTokenCall(
        td: TokenData
    ): Call<ResponseBody> {
        return Sahha.di.api.postRefreshToken(td)
    }

    internal suspend fun storeNewTokens(responseBody: ResponseBody?) {
        val json = SahhaConverterUtility.responseBodyToJson(responseBody)
        json?.also {
            Sahha.di.encryptor.encryptText(Constants.UET, it["profileToken"].toString())
            Sahha.di.encryptor.encryptText(Constants.UERT, it["refreshToken"].toString())
        }
    }

    internal suspend fun postRefreshToken(retryLogic: (suspend () -> Unit)) {
        val tokenData = TokenData(
            Sahha.di.decryptor.decrypt(Constants.UET),
            Sahha.di.decryptor.decrypt(Constants.UERT)
        )

        try {
            val call = getRefreshTokenCall(tokenData)
            call.enqueue(
                object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        Sahha.di.mainScope.launch {
                            if (ResponseCode.isSuccessful(response.code())) {
                                storeNewTokens(response.body())
                                retryLogic()
                                return@launch
                            }

                            Sahha.di.sahhaErrorLogger.api(
                                call,
                                SahhaErrors.typeAuthentication,
                                response.code(),
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Sahha.di.sahhaErrorLogger.api(
                            call,
                            SahhaErrors.typeAuthentication,
                            null,
                            t.message ?: SahhaErrors.responseFailure
                        )
                    }

                }
            )
        } catch (e: Exception) {
            Sahha.di.sahhaErrorLogger.application(
                e.message ?: "Error refreshing token",
                "postRefreshToken",
                null
            )
        }
    }
}