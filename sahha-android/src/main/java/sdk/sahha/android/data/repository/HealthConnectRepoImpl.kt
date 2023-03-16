package sdk.sahha.android.data.repository

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import retrofit2.Call
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaResponseHandler
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.TokenBearer
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

    override suspend fun getStepData(
        timeRangeFilter: TimeRangeFilter
    ): List<StepsRecord> {
        return healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = timeRangeFilter,
            )
        ).records
    }

    override suspend fun getSleepData(
        timeRangeFilter: TimeRangeFilter,
    ): List<SleepSessionRecord> {
        return healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = timeRangeFilter
            )
        ).records
    }

    override suspend fun getSleepStageData(
        timeRangeFilter: TimeRangeFilter,
    ): List<SleepStageRecord> {
        return healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepStageRecord::class,
                timeRangeFilter = timeRangeFilter
            )
        ).records
    }

    override suspend fun getHeartRateData(
        timeRangeFilter: TimeRangeFilter,
    ): List<HeartRateRecord> {
        return healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = timeRangeFilter
            )
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

    override suspend fun postSleepSessions(
        timeRangeFilter: TimeRangeFilter,
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        try {
            val call = getSleepDataCall(timeRangeFilter)
            SahhaResponseHandler.handleResponse(
                call, { getSleepDataCall(timeRangeFilter) }, callback
            ) { callback(null, true) }
        } catch (e: Exception) {
            sahhaErrorLogger.application(
                e.message,
                "postSleepSessions",
                getSleepData(timeRangeFilter).toString()
            )
        }
    }

    private suspend fun getSleepDataCall(
        timeRangeFilter: TimeRangeFilter
    ): Call<ResponseBody> {
        return api.postSleepDataRange(
            tokenBearer,
            SahhaConverterUtility.sleepSessionToSleepDto(
                getSleepData(timeRangeFilter), timeManager.nowInISO()
            ).map { it.toSleepSendDto() }
        )
    }

    override suspend fun postSleepStages(
        timeRangeFilter: TimeRangeFilter,
        callback: (error: String?, successful: Boolean) -> Unit
    ) {
        try {
            val call = getSleepStageDataCall(timeRangeFilter)
            SahhaResponseHandler.handleResponse(
                call, { getSleepStageDataCall(timeRangeFilter) }, callback
            ) {
                callback(null, true)
            }
        } catch (e: Exception) {
            sahhaErrorLogger.application(
                e.message,
                "postSleepStages",
                getSleepStageData(timeRangeFilter).toString()
            )
        }
    }

    private suspend fun getSleepStageDataCall(timeRangeFilter: TimeRangeFilter): Call<ResponseBody> {
        return api.postSleepDataRange(
            tokenBearer,
            SahhaConverterUtility.sleepStageToSleepDto(
                getSleepStageData(timeRangeFilter), timeManager.nowInISO()
            ).map { it.toSleepSendDto() }
        )
    }

    override suspend fun postSteps(
        timeRangeFilter: TimeRangeFilter, callback: (error: String?, successful: Boolean) -> Unit
    ) {
        try {
            val call = getStepDataCall(timeRangeFilter)
            SahhaResponseHandler.handleResponse(
                call, { getStepDataCall(timeRangeFilter) }, callback
            ) {
                callback(null, true)
            }
        } catch (e: Exception) {
            sahhaErrorLogger.application(
                e.message,
                "postSteps",
                getStepDataCall(timeRangeFilter).toString()
            )
        }
    }

    private suspend fun getStepDataCall(timeRangeFilter: TimeRangeFilter): Call<ResponseBody> {
        return api.postStepData(
            tokenBearer,
            SahhaConverterUtility.healthConnectStepToStepDto(
                getStepData(timeRangeFilter), timeManager.nowInISO()
            )
        )
    }

    override suspend fun postHeartRates(
        timeRangeFilter: TimeRangeFilter,
        callback: (error: String?, successful: Boolean) -> Unit
    ) {
        try {
            val call = getHeartRateDataCall(timeRangeFilter)
            SahhaResponseHandler.handleResponse(
                call, { getHeartRateDataCall(timeRangeFilter) }, callback
            ) {
                callback(null, true)
            }
        } catch (e: Exception) {
            sahhaErrorLogger.application(
                e.message,
                "postHeartRates",
                getHeartRateDataCall(timeRangeFilter).toString()
            )
        }
    }

    private suspend fun getHeartRateDataCall(timeRangeFilter: TimeRangeFilter): Call<ResponseBody> {
        return api.postHeartRateRange(
            tokenBearer,
            SahhaConverterUtility.heartRateToHeartRateSendDto(
                getHeartRateData(timeRangeFilter), timeManager.nowInISO()
            )
        )
    }
}