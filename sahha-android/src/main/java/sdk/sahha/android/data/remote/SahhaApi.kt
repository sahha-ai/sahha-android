package sdk.sahha.android.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import sdk.sahha.android.common.Constants.APP_ID_HEADER
import sdk.sahha.android.common.Constants.APP_SECRET_HEADER
import sdk.sahha.android.common.Constants.AUTHORIZATION_HEADER
import sdk.sahha.android.domain.model.analyze.AnalyzeRequest
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.model.dto.DemographicDto
import sdk.sahha.android.domain.model.dto.SahhaDataLogDto
import sdk.sahha.android.domain.model.dto.send.*
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.source.SahhaDemographic
internal interface SahhaApi {
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

    @POST("profile/health/log")
    suspend fun postStepDataLog(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body stepData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postSleepDataRange(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body sleepData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postHeartRateData(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body heartRateData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postBloodGlucoseData(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body bloodData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postBloodPressureData(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body bloodData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postDeviceActivityRange(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body lockData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postActiveCaloriesBurned(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body activeCaloriesBurnedData: List<SahhaDataLogDto?>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postTotalCaloriesBurned(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body totalCaloriesBurnedData: List<SahhaDataLogDto?>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postBasalMetabolicRate(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body basalMetabolicRateData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postOxygenSaturation(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body oxygenSaturationData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postVo2Max(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body vo2MaxData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postRespiratoryRate(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body respiratoryRate: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postBodyFat(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body bodyFatData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postBodyWaterMass(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body bodyWaterMassData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postLeanBodyMass(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body leanBodyMassData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postBoneMass(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body boneMassData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postHeight(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body heightData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postWeight(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body weightData: List<SahhaDataLogDto>
    ): Response<ResponseBody>

    @POST("profile/health/log")
    suspend fun postSahhaDataLogs(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body sahhaDataLogs: List<SahhaDataLogDto>
    ): Response<ResponseBody>



    @POST("profile/analysis")
    fun analyzeProfile(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
    ): Call<ResponseBody>

    @POST("profile/analysis")
    fun analyzeProfile(
        @Header(AUTHORIZATION_HEADER) profileToken: String,
        @Body analyzeRequest: AnalyzeRequest
    ): Call<ResponseBody>

    @POST("profile/analysis")
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

