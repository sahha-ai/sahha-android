package sdk.sahha.android.common

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface TestApi {
    @GET("spells/acid-arrow")
      fun getTestCall(): Call<ResponseBody>

    @GET("spells/acid-arrow")
      fun getTestResponse(): Response<ResponseBody>

     @GET("spells/acid-arrow")
      fun getTestJson(): Response<JSONObject>

     @GET("spells/acid-arrow")
      fun getTestString(): Response<String>
}