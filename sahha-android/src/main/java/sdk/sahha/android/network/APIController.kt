package sdk.sahha.android.network

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import sdk.sahha.android.utils.TimeController
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

@RequiresApi(Build.VERSION_CODES.N)
class APIController {
  private val tag = "APIController"
  private val tc by lazy { TimeController() }

  // TODO: Switch between dev/prod environment
  private val retrofit by lazy {
    Retrofit.Builder()
      .baseUrl("https://dev-api.sahha.ai/api/")
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

  val service by lazy {
    retrofit.create(APIService::class.java)
  }

  fun loginUser(username: String, password: String, context: Context) {
    val requestBody =
      buildRequestBody(hashMapOf("username" to username, "password" to password))

    val call = service.loginUser(requestBody)
    println("DEBUG: Received call: $call")

    call.enqueue(object : Callback<ResponseBody> {
      override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//        Toast.makeText(context, "onResponse: ${response.raw()}", Toast.LENGTH_SHORT).show()
        Log.w(tag, "onResponse: ${response.raw()}")
        if (response.code() == 200) {
          println("Success: ${response.raw()}")
          Toast.makeText(context, "Successful: ${response.code()}", Toast.LENGTH_LONG).show()
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
