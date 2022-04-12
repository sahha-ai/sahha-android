package sdk.sahha.android.data.repository

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.Constants.CET
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.repository.AuthRepo
import javax.inject.Inject
import javax.inject.Named

class AuthRepoImpl @Inject constructor(
    private val context: Context,
    private val api: SahhaApi,
    @Named("ioScope") private val ioScope: CoroutineScope,
    @Named("mainScope") private val mainScope: CoroutineScope,
    private val securityDao: SecurityDao
) : AuthRepo {
    override suspend fun authenticate(
        profileId: String,
        callback: ((error: String?, success: String?) -> Unit)
    ) {
        try {
            val response = api.profileAuth(TokenBearer(decryptTokenAsync(CET)), profileId)
            if (ResponseCode.isSuccessful(response.code())) {
                val token = getTokenFromResponse(response)
                storeToken(token)
                callback(null, "${response.code()}: ${response.message()}")
                return
            }

            callback("${response.code()}: ${response.message()}", null)
        } catch (e: Exception) {
            callback(e.message, null)
        }
    }

    private fun getTokenFromResponse(response: Response<ResponseBody>): String {
        val jsonObject = JSONObject(response.body()!!.string())
        return jsonObject["token"] as String
    }

    private fun storeToken(token: String) {
        ioScope.launch {
            encryptTokenAsync(token)
        }
    }

    private suspend fun encryptTokenAsync(token: String) {
        Encryptor(securityDao).encryptText(UET, token)
    }

    private suspend fun decryptTokenAsync(alias: String): String {
        return Decryptor(securityDao).decrypt(alias)
    }
}