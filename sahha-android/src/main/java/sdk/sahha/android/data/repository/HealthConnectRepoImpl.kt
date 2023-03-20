package sdk.sahha.android.data.repository

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.Call
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaResponseHandler
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.source.HealthConnectSensor
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.dto.toSleepSendDto
import sdk.sahha.android.domain.model.config.LastHealthConnectPost
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.SahhaConverterUtility

class HealthConnectRepoImpl(
    private val healthConnectClient: HealthConnectClient,
    private val timeManager: SahhaTimeManager,
    private val configurationDao: ConfigurationDao,
    private val api: SahhaApi,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val decryptor: Decryptor
) : HealthConnectRepo {
    private val tokenBearer by lazy { runBlocking { TokenBearer(decryptor.decrypt(UET)) } }

    override suspend fun <T : Record> getSensorData(
        readRecordsRequest: ReadRecordsRequest<T>
    ): List<T> {
        return healthConnectClient.readRecords(
            readRecordsRequest
        ).records
    }

    override suspend fun getGrantedPermissions(): Set<Permission> {
        return healthConnectClient.permissionController.getGrantedPermissions(
            setOf(
                Permission.createReadPermission(SleepSessionRecord::class),
                Permission.createReadPermission(SleepStageRecord::class),
                Permission.createReadPermission(StepsRecord::class),
                Permission.createReadPermission(HeartRateRecord::class),
            )
        )
    }

    override suspend fun getLastPostTimestamp(sensorId: Int): Long? {
        return configurationDao.getLastHealthConnectPost(sensorId)
    }

    override suspend fun saveLastPost(
        sensorId: Int,
        timestamp: Long,
    ) {
        configurationDao.saveLastHealthConnectPost(
            LastHealthConnectPost(
                id = sensorId,
                epochMillis = timestamp
            )
        )
    }

    override suspend fun postHealthConnectData(
        healthConnectSensor: Enum<HealthConnectSensor>,
        timeRangeFilter: TimeRangeFilter,
        callback: (error: String?, successful: Boolean) -> Unit
    ) {
        when (healthConnectSensor) {
            HealthConnectSensor.sleep_session -> postSleepSessions(timeRangeFilter, callback)
            HealthConnectSensor.sleep_stage -> postSleepStages(timeRangeFilter, callback)
            HealthConnectSensor.step -> postSteps(timeRangeFilter, callback)
            HealthConnectSensor.heart_rate -> postHeartRates(timeRangeFilter, callback)
        }
    }

    private suspend fun postSleepSessions(
        timeRangeFilter: TimeRangeFilter,
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        try {
            val call = getSleepDataCall(timeRangeFilter)
            SahhaResponseHandler.handleResponse(
                call, { getSleepDataCall(timeRangeFilter) }, callback
            )
        } catch (e: Exception) {
            sahhaErrorLogger.application(
                e.message,
                "postSleepSessions",
                getSensorData(
                    ReadRecordsRequest(
                        SleepSessionRecord::class,
                        timeRangeFilter
                    )
                ).toString()
            )
            callback(e.message, false)
        }
    }

    private suspend fun getSleepDataCall(
        timeRangeFilter: TimeRangeFilter
    ): Call<ResponseBody> {
        return api.postSleepDataRange(
            tokenBearer,
            SahhaConverterUtility.sleepSessionToSleepDto(
                getSensorData(ReadRecordsRequest(SleepSessionRecord::class, timeRangeFilter)),
                timeManager.nowInISO()
            ).map { it.toSleepSendDto() }
        )
    }

    private suspend fun postSleepStages(
        timeRangeFilter: TimeRangeFilter,
        callback: (error: String?, successful: Boolean) -> Unit
    ) {
        try {
            val call = getSleepStageDataCall(timeRangeFilter)
            SahhaResponseHandler.handleResponse(
                call, { getSleepStageDataCall(timeRangeFilter) }, callback
            )
        } catch (e: Exception) {
            sahhaErrorLogger.application(
                e.message,
                "postSleepStages",
                getSensorData(
                    ReadRecordsRequest(
                        SleepStageRecord::class,
                        timeRangeFilter
                    )
                ).toString()
            )
            callback(e.message, false)
        }
    }

    private suspend fun getSleepStageDataCall(timeRangeFilter: TimeRangeFilter): Call<ResponseBody> {
        return api.postSleepDataRange(
            tokenBearer,
            SahhaConverterUtility.sleepStageToSleepDto(
                getSensorData(ReadRecordsRequest(SleepStageRecord::class, timeRangeFilter)),
                timeManager.nowInISO()
            ).map { it.toSleepSendDto() }
        )
    }

    private suspend fun postSteps(
        timeRangeFilter: TimeRangeFilter, callback: (error: String?, successful: Boolean) -> Unit
    ) {
        try {
            val call = getStepDataCall(timeRangeFilter)
            SahhaResponseHandler.handleResponse(
                call, { getStepDataCall(timeRangeFilter) }, callback
            )
        } catch (e: Exception) {
            sahhaErrorLogger.application(
                e.message,
                "postSteps",
                getSensorData(ReadRecordsRequest(StepsRecord::class, timeRangeFilter)).toString()
            )
            callback(e.message, false)
        }
    }

    private suspend fun getStepDataCall(timeRangeFilter: TimeRangeFilter): Call<ResponseBody> {
        return api.postStepData(
            tokenBearer,
            SahhaConverterUtility.healthConnectStepToStepDto(
                getSensorData(
                    ReadRecordsRequest(
                        StepsRecord::class,
                        timeRangeFilter
                    )
                ), timeManager.nowInISO()
            )
        )
    }

    private suspend fun postHeartRates(
        timeRangeFilter: TimeRangeFilter,
        callback: (error: String?, successful: Boolean) -> Unit
    ) {
        try {
            val call = getHeartRateDataCall(timeRangeFilter)
            SahhaResponseHandler.handleResponse(
                call, { getHeartRateDataCall(timeRangeFilter) }, callback
            )
        } catch (e: Exception) {
            sahhaErrorLogger.application(
                e.message,
                "postHeartRates",
                getSensorData(
                    ReadRecordsRequest(
                        HeartRateRecord::class,
                        timeRangeFilter
                    )
                ).toString()
            )
            callback(e.message, false)
        }
    }

    private suspend fun getHeartRateDataCall(timeRangeFilter: TimeRangeFilter): Call<ResponseBody> {
        return api.postHeartRateRange(
            tokenBearer,
            SahhaConverterUtility.heartRateToHeartRateSendDto(
                getSensorData(ReadRecordsRequest(HeartRateRecord::class, timeRangeFilter)),
                timeManager.nowInISO()
            )
        )
    }
}