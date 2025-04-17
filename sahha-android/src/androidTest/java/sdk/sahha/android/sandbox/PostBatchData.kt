package sdk.sahha.android.sandbox

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.work.ListenableWorker.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.dto.DemographicDto
import sdk.sahha.android.domain.model.dto.send.DeviceInformationDto
import sdk.sahha.android.domain.model.dto.send.ExternalIdSendDto
import sdk.sahha.android.domain.model.dto.send.RefreshTokenSendDto
import sdk.sahha.android.domain.model.dto.send.SahhaDataLogDto
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.domain.use_case.CalculateBatchLimit
import sdk.sahha.android.domain.use_case.background.FilterActivityOverlaps
import sdk.sahha.android.domain.use_case.metadata.AddMetadata
import sdk.sahha.android.domain.use_case.post.PostBatchData
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings
import java.util.UUID
import kotlin.coroutines.resume

internal class PostBatchData {
    companion object {
        lateinit var context: Context

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            ActivityScenario.launch(ComponentActivity::class.java).onActivity { activity ->
                context = activity.applicationContext
                val settings = SahhaSettings(SahhaEnvironment.sandbox)
                launch {
                    SahhaSetupUtil.configureSahha(activity, settings)
//                    SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
//                    SahhaSetupUtil.deauthenticateSahha()

//                    Sahha.di.batchedDataRepo.saveBatchedData(data)
                }
            }
        }

        private suspend fun getMockBatchData(amount: Int): List<SahhaDataLog> {
            val jobs = mutableListOf<Deferred<Unit>>()
            val data = mutableListOf<SahhaDataLog>()
            for (i in 0 until amount) {
                jobs += CoroutineScope(Dispatchers.Default).async {
                    Sahha.di.batchedDataRepo.saveBatchedData(
                        listOf(
                            SahhaDataLog(
                                id = UUID.randomUUID().toString(),
                                logType = Constants.DataLogs.ACTIVITY,
                                dataType = SahhaSensor.steps.name,
                                value = i.toDouble(),
                                source = "sahha.beast.wars",
                                startDateTime = Sahha.di.timeManager.nowInISO(),
                                endDateTime = Sahha.di.timeManager.nowInISO(),
                                unit = Constants.DataUnits.COUNT,
                                deviceId = null
                            )
                        )
                    )
                }
                println("Created log $i")
            }
            jobs.awaitAll()
            return data
        }
    }

    val mockPostBatchData = PostBatchData(
        context = context,
        api = object : SahhaApi {
            override suspend fun postProfileIdForToken(profileId: String): Response<TokenData> {
                TODO("Not yet implemented")
            }

            override suspend fun postExternalIdForToken(
                appId: String,
                appSecret: String,
                externalId: ExternalIdSendDto
            ): Response<TokenData> {
                TODO("Not yet implemented")
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
                val mediaType = "text/plain".toMediaTypeOrNull()
                val responseBody =
                    "Mock response for postSahhaDataLogs".toResponseBody(mediaType)
                println("Mock post OK")
                return Response.success(responseBody)
            }

            override suspend fun postSahhaDataLogDto(
                profileToken: String,
                sahhaDataLogDto: List<SahhaDataLogDto>
            ): Response<ResponseBody> {
                TODO("Not yet implemented")
            }

            override fun getDemographic(profileToken: String): Call<DemographicDto> {
                TODO("Not yet implemented")
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

        },
        chunkManager = Sahha.di.postChunkManager,
        authRepo = Sahha.di.authRepo,
        batchRepo = Sahha.di.batchedDataRepo,
        sahhaErrorLogger = Sahha.di.sahhaErrorLogger,
        calculateBatchLimit = CalculateBatchLimit(Sahha.di.batchedDataRepo),
        filterActivityOverlaps = FilterActivityOverlaps(Sahha.di.batchedDataRepo, Sahha.di.timeManager),
        addMetadata = AddMetadata(Sahha.di.timeManager),
        dataLogTransformer = Sahha.di.dataLogTransformer
    )

    @Test
    fun saveHundredThousandLogs_isSuccessful() = runTest {
        Sahha.di.batchedDataRepo.deleteAllBatchedData()
        getMockBatchData(100000)

        val logs = Sahha.di.batchedDataRepo.getBatchedData()
        Assert.assertEquals(100000, logs.count())
    }

    @Test
    fun postFiftyThousandLogs_isSuccessful() = runTest {
        val result = suspendCancellableCoroutine { cont ->
            val deferred = mutableListOf<Deferred<Unit>>()
            CoroutineScope(Dispatchers.Default).launch {
                do {
                    deferred += async {
                        val batchedData = try {
                            val data = Sahha.di.batchedDataRepo.getBatchedData()
                            println(data.count())
                            data
                        } catch (e: Exception) {
                            if (cont.isActive) cont.resume(Result.retry())
                            emptyList()
                        }
                        withTimeout(Constants.POST_TIMEOUT_LIMIT_MILLIS) {
                            mockPostBatchData(
                                batchedData
                            ) { _, _ -> }
                        }
                    }
                } while (Sahha.di.batchedDataRepo.getBatchedData().isNotEmpty())
                deferred.awaitAll()
                cont.resume(Unit)
            }
        }

//        Assert.assertEquals(Result.success(), result)
    }
}