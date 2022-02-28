package sdk.sahha.android.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface APIService {
  @POST("auth/login")
  fun loginUser(
    @Body requestBody: RequestBody,
  ): Call<ResponseBody>
}
