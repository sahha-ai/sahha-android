package sdk.sahha.android.controller.network

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sdk.sahha.android.data.UET
import sdk.sahha.android.controller.utils.TimeController
import sdk.sahha.android.controller.utils.security.Encryptor

@RequiresApi(Build.VERSION_CODES.N)
object SahhaAPIController {
    private val tag = "APIController"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://sahhaapi-sandbox.azurewebsites.net/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service by lazy {
        retrofit.create(APIService::class.java)
    }

    fun authentication(customerId: String, profileId: String, context: Context) {
        val ioScope = CoroutineScope(IO)
        val call = service.authentication(customerId, profileId)
        println("DEBUG: Received call: $call")

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//        Toast.makeText(context, "onResponse: ${response.raw()}", Toast.LENGTH_SHORT).show()
                Log.w(tag, "onResponse: ${response.raw()}")
                if (response.code() == 200) {
                    Toast.makeText(context, "Successful: ${response.code()}", Toast.LENGTH_LONG)
                        .show()

                    response.body()?.let { responseBody ->
                        val jsonObject = JSONObject(responseBody.string())
                        val token = jsonObject["token"] as String

                        ioScope.launch {
                            Encryptor(context).encryptText(UET, token)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(tag, t.message, t)
            }
        })
    }

    private fun buildRequestBody(rbContent: HashMap<String, Any>): RequestBody {
        val jsonObject = JSONObject()

        for (content in rbContent) {
            jsonObject.put(content.key, content.value)
        }

        val jsonString = jsonObject.toString()
        val requestBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

        return requestBody
    }
}
