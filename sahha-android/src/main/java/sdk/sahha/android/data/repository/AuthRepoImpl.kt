package sdk.sahha.android.data.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.repository.AuthRepo
import javax.inject.Inject

class AuthRepoImpl @Inject constructor(
    private val context: Context,
    private val api: SahhaApi,
    private val ioScope: CoroutineScope,
    private val securityDao: SecurityDao
) : AuthRepo {
    private val tag = "AuthRepoImpl"

    override fun authenticate(customerId: String, profileId: String) {
        val call = api.authenticate(customerId, profileId)
        enqueueCall(call)
    }

    private fun enqueueCall(call: Call<ResponseBody>) {
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (ResponseCode.isSuccessful(response.code())) {
                    displayToast("Successful: ${response.code()}")
                    storeToken(response)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                displayToast("Failure: ${t.message}")
                Log.e(tag, t.message, t)
            }
        })
    }

    private fun storeToken(response: Response<ResponseBody>) {
        response.body()?.let { responseBody ->
            val jsonObject = JSONObject(responseBody.string())
            val token = jsonObject["token"] as String

            ioScope.launch {
                encryptTokenAsync(token)
            }
        }
    }

    private suspend fun encryptTokenAsync(token: String) {
        Encryptor(securityDao).encryptText(UET, token)
    }

    private fun displayToast(toastMsg: String) {
        Toast.makeText(context, toastMsg, Toast.LENGTH_LONG)
            .show()
    }
}