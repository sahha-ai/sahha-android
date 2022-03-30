package sdk.sahha.android.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import sdk.sahha.android.data.remote.dto.SleepDto

interface SahhaApi {
    @POST("authentication")
    fun authenticate(
        @Query("customerId", encoded = true) customerId: String,
        @Query("profileId", encoded = true) profileId: String
    ): Call<ResponseBody>

    @POST("sleep/logRange")
    fun sendSleepDataRange(
        @Header("Authorization") token: String,
        @Body sleepData: List<SleepDto>
    ): Call<ResponseBody>
}
