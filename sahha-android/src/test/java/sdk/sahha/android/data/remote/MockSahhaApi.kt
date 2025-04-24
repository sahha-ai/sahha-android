package sdk.sahha.android.data.remote

import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.trimSubstring
import org.mockito.Mockito
import retrofit2.Call
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.dto.DemographicDto
import sdk.sahha.android.domain.model.dto.send.DeviceInformationDto
import sdk.sahha.android.domain.model.dto.send.ExternalIdSendDto
import sdk.sahha.android.domain.model.dto.send.RefreshTokenSendDto
import sdk.sahha.android.domain.model.dto.send.SahhaDataLogDto
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.source.SahhaDemographic

internal class MockSahhaApi(
    private val delegate: BehaviorDelegate<SahhaApi>
) : SahhaApi {
    override suspend fun postProfileIdForToken(profileId: String): Response<TokenData> {
        val tokenData = TokenData(
            "mock_profile_token",
            "mock_refresh_token"
        )
        return delegate.returningResponse(tokenData).postProfileIdForToken(profileId)
    }

    override suspend fun postExternalIdForToken(
        appId: String,
        appSecret: String,
        externalId: ExternalIdSendDto
    ): Response<TokenData> {
        val tokenData = TokenData(
            "${appId.trimSubstring(0, 5)}_${
                appSecret.trimSubstring(0, 5)
            }_${externalId.externalId.trimSubstring(0, 5)}",
            "mock_refresh_token"
        )
        return delegate.returningResponse(tokenData)
            .postExternalIdForToken(appId, appSecret, externalId)
    }

    override fun postRefreshToken(tokenData: TokenData): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postRefreshTokenResponse(
        profileToken: String,
        refreshToken: RefreshTokenSendDto
    ): Response<TokenData> {
        TODO("Not yet implemented")
    }

    override suspend fun postStepDataLog(
        profileToken: String,
        stepData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postSleepDataRange(
        profileToken: String,
        sleepData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postHeartRateData(
        profileToken: String,
        heartRateData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postBloodGlucoseData(
        profileToken: String,
        bloodData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postBloodPressureData(
        profileToken: String,
        bloodData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postDeviceActivityRange(
        profileToken: String,
        lockData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postActiveCaloriesBurned(
        profileToken: String,
        activeCaloriesBurnedData: List<SahhaDataLog?>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postTotalCaloriesBurned(
        profileToken: String,
        totalCaloriesBurnedData: List<SahhaDataLog?>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postBasalMetabolicRate(
        profileToken: String,
        basalMetabolicRateData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postOxygenSaturation(
        profileToken: String,
        oxygenSaturationData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postVo2Max(
        profileToken: String,
        vo2MaxData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postRespiratoryRate(
        profileToken: String,
        respiratoryRate: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postBodyFat(
        profileToken: String,
        bodyFatData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postBodyWaterMass(
        profileToken: String,
        bodyWaterMassData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postLeanBodyMass(
        profileToken: String,
        leanBodyMassData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postBoneMass(
        profileToken: String,
        boneMassData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postHeight(
        profileToken: String,
        heightData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postWeight(
        profileToken: String,
        weightData: List<SahhaDataLog>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postSahhaDataLogs(
        profileToken: String,
        sahhaDataLogs: List<SahhaDataLog>
    ): Response<ResponseBody> {
        val responseBody = """{"key":"value"}""".toResponseBody()

        val response =
            if (profileToken.isNotEmpty() && sahhaDataLogs.isNotEmpty())
                Response.success(responseBody)
            else
                Response.error(500, responseBody)

        return delegate.returningResponse(response).postSahhaDataLogs(profileToken, sahhaDataLogs)
    }

    override suspend fun postSahhaDataLogDto(
        profileToken: String,
        sahhaDataLogDto: List<SahhaDataLogDto>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override fun getDemographic(profileToken: String): Call<DemographicDto> {
        return delegate.returning(Mockito.mock(Call::class.java) as Call<DemographicDto>).getDemographic(profileToken)
    }

    override fun patchDemographic(
        profileToken: String,
        demographics: SahhaDemographic
    ): Call<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun putDeviceInformation(
        profileToken: String,
        deviceInformation: DeviceInformationDto
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun postInsightsData(
        profileToken: String,
        insights: List<InsightData>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun getScores(
        profileToken: String,
        types: List<String>
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun getScores(
        profileToken: String,
        types: List<String>,
        startDateTimeIso: String,
        endDateTimeIso: String
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }

    override suspend fun getBiomarkers(
        profileToken: String,
        categories: List<String>,
        types: List<String>,
        startDateTimeIso: String?,
        endDateTimeIso: String?
    ): Response<ResponseBody> {
        TODO("Not yet implemented")
    }
}