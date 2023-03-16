package sdk.sahha.android.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import sdk.sahha.android.data.Constants.AUTHORIZATION_HEADER
import sdk.sahha.android.data.remote.dto.DemographicDto
import sdk.sahha.android.data.remote.dto.send.*
import sdk.sahha.android.domain.model.analyze.AnalyzeRequest
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.source.SahhaDemographic

interface SahhaApi {
    @POST("oauth/profile/refreshToken")
    fun postRefreshToken(
        @Body tokenData: TokenData
    ): Call<ResponseBody>

    @POST("movement/log")
    fun postStepData(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body stepData: List<StepSendDto>
    ): Call<ResponseBody>

    @POST("sleep/log")
    fun postSleepDataRange(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body sleepData: List<SleepSendDto>
    ): Call<ResponseBody>

    @POST("heartRate/log")
    fun postHeartRateRange(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body heartRateData: List<HeartRateSendDto>
    ): Call<ResponseBody>

    @POST("deviceActivity/lock")
    fun postDeviceActivityRange(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body lockData: List<PhoneUsageSendDto>
    ): Call<ResponseBody>

    @POST("profile/analyze")
    fun analyzeProfile(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
    ): Call<ResponseBody>

    @POST("profile/analyze")
    fun analyzeProfile(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body analyzeRequest: AnalyzeRequest
    ): Call<ResponseBody>

    @GET("profile/demographic")
    fun getDemographic(
        @Header(AUTHORIZATION_HEADER) profileToken: String
    ): Call<DemographicDto>

    @PUT("profile/demographic")
    fun putDemographic(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body demographics: SahhaDemographic
    ): Call<ResponseBody>

    @PUT("profile/deviceInformation")
    fun putDeviceInformation(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body deviceInformation: DeviceInformationSendDto
    ): Call<ResponseBody>
}
