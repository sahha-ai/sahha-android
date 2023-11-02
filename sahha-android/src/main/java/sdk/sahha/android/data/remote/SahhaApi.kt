package sdk.sahha.android.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import sdk.sahha.android.data.Constants.APP_ID_HEADER
import sdk.sahha.android.data.Constants.APP_SECRET_HEADER
import sdk.sahha.android.data.Constants.AUTHORIZATION_HEADER
import sdk.sahha.android.domain.model.analyze.AnalyzeRequest
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.model.dto.BloodGlucoseDto
import sdk.sahha.android.domain.model.dto.BloodPressureDto
import sdk.sahha.android.domain.model.dto.DemographicDto
import sdk.sahha.android.domain.model.dto.HeartRateDto
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.domain.model.dto.send.*
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.source.SahhaDemographic

interface SahhaApi {
    @POST("oauth/profile/token")
    suspend fun postProfileIdForToken(
        @Body profileId: String
    ): Response<TokenData>

    @POST("oauth/profile/register/appId")
    suspend fun postExternalIdForToken(
        @Header(APP_ID_HEADER) appId: String,
        @Header(APP_SECRET_HEADER) appSecret: String,
        @Body externalId: ExternalIdSendDto
    ): Response<TokenData>

    @POST("oauth/profile/refreshToken")
    fun postRefreshToken(
        @Body tokenData: TokenData
    ): Call<ResponseBody>

    @POST("oauth/profile/refreshToken")
    suspend fun postRefreshTokenResponse(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body refreshToken: RefreshTokenSendDto
    ): Response<TokenData>

    @POST("profile/movement/log")
    suspend fun postStepData(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body stepData: List<StepDto>
    ): Response<ResponseBody>

    @POST("profile/sleep/log")
    suspend fun postSleepDataRange(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body sleepData: List<SleepSendDto>
    ): Response<ResponseBody>

    @POST("profile/heart/log")
    suspend fun postHeartRateData(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body heartRateData: List<HeartRateDto>
    ): Response<ResponseBody>

    @POST("profile/blood/log")
    suspend fun postBloodGlucoseData(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body bloodData: List<BloodGlucoseDto>
    ): Response<ResponseBody>

    @POST("profile/blood/log")
    suspend fun postBloodPressureData(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body bloodData: List<BloodPressureDto>
    ): Response<ResponseBody>

    @POST("profile/device/log")
    suspend fun postDeviceActivityRange(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body lockData: List<PhoneUsageSendDto>
    ): Response<ResponseBody>

    @POST("profile/analyze")
    fun analyzeProfile(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
    ): Call<ResponseBody>

    @POST("profile/analyze")
    fun analyzeProfile(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body analyzeRequest: AnalyzeRequest
    ): Call<ResponseBody>

    @POST("profile/analyze")
    suspend fun analyzeProfileResponse(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body analyzeRequest: AnalyzeRequest
    ): Response<ResponseBody>

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
    suspend fun putDeviceInformation(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body deviceInformation: DeviceInformationDto
    ): Response<ResponseBody>

    @POST("profile/insight")
    suspend fun postInsightsData(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body insights: List<InsightData>
    ): Response<ResponseBody>
}
