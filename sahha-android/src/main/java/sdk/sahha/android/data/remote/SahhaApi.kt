package sdk.sahha.android.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import sdk.sahha.android.data.Constants.AUTHORIZATION_HEADER
import sdk.sahha.android.data.remote.dto.DemographicDto
import sdk.sahha.android.data.remote.dto.SleepDto
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.source.SahhaDemographic

interface SahhaApi {
    @POST("oauth/profile/refresh-token")
    suspend fun postRefreshToken(
        @Body tokenData: TokenData
    ): Response<ResponseBody>

    @POST("sleep/logRange")
    suspend fun postSleepDataRange(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body sleepData: List<SleepDto>
    ): Response<ResponseBody>

    @POST("deviceActivity/lockRange")
    suspend fun postDeviceActivityRange(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body lockData: List<PhoneUsage>
    ): Response<ResponseBody>

    @GET("profile/analyze")
    suspend fun analyzeProfile(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
    ): Response<ResponseBody>

    @GET("profile/demographic")
    suspend fun getDemographic(
        @Header(AUTHORIZATION_HEADER) profileToken: String
    ): Response<DemographicDto>

    @PUT("profile/demographic")
    suspend fun postDemographic(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body demographics: SahhaDemographic
    ): Response<ResponseBody>
}
