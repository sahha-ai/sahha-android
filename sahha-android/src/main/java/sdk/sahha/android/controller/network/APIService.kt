package sdk.sahha.android.controller.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface APIService {
    @POST("authentication")
    fun authentication(
        @Query("customerId", encoded = true) customerId: String,
        @Query("profileId", encoded = true) profileId: String
    ): Call<ResponseBody>
}
