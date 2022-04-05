package sdk.sahha.android.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import sdk.sahha.android.data.Constants.AUTHORIZATION_HEADER
import sdk.sahha.android.data.remote.dto.DemographicDto
import sdk.sahha.android.data.remote.dto.SleepDto
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.profile.SahhaDemographic

interface SahhaApi {
    @POST("authentication")
    fun authenticate(
        @Query("customerId", encoded = true) customerId: String,
        @Query("profileId", encoded = true) profileId: String
    ): Call<ResponseBody>

    @POST("sleep/logRange")
    fun postSleepDataRange(
        @Header(AUTHORIZATION_HEADER) token: String,
        @Body sleepData: List<SleepDto>
    ): Call<ResponseBody>

    @POST("deviceActivity/lockRange")
    fun postDeviceActivityRange(
        @Header(AUTHORIZATION_HEADER) token: String,
        @Body lockData: List<PhoneUsage>
    ): Call<ResponseBody>

    @GET("profile/analyze")
    suspend fun analyzeProfile(
        @Header(AUTHORIZATION_HEADER) token: String,
    ): Response<ResponseBody>

    @GET("profile/demographic")
    suspend fun getDemographic(
        @Header(AUTHORIZATION_HEADER) token: String
    ): Response<DemographicDto>

    @PUT("profile/demographic")
    suspend fun postDemographic(
        @Header(AUTHORIZATION_HEADER) token: String,
        @Body demographics: SahhaDemographic
    ): Response<ResponseBody>
}
