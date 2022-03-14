package sdk.sahha.android._refactor.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface SahhaApi {
    @POST("authentication")
    fun authenticate(
        @Query("customerId", encoded = true) customerId: String,
        @Query("profileId", encoded = true) profileId: String
    ): Call<ResponseBody>
}
