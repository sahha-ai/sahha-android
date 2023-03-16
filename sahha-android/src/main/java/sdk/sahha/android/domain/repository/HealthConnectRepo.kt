package sdk.sahha.android.domain.repository

import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.common.enums.HealthConnectSensor
import sdk.sahha.android.domain.model.config.LastHealthConnectPost

interface HealthConnectRepo {
    suspend fun getStepData(timeRangeFilter: TimeRangeFilter): List<StepsRecord>
    suspend fun getSleepData(timeRangeFilter: TimeRangeFilter): List<SleepSessionRecord>
    suspend fun getSleepStageData(timeRangeFilter: TimeRangeFilter): List<SleepStageRecord>
    suspend fun getHeartRateData(timeRangeFilter: TimeRangeFilter): List<HeartRateRecord>
    suspend fun getGrantedPermissions(): Set<Permission>
    suspend fun getLastPostTimestamp(sensorId: Int): Long?
    suspend fun saveLastPost(sensorId: Int, timestamp: Long)
    suspend fun postSleepSessions(
        timeRangeFilter: TimeRangeFilter,
        callback: ((error: String?, successful: Boolean) -> Unit)
    )

    suspend fun postSleepStages(
        timeRangeFilter: TimeRangeFilter,
        callback: ((error: String?, successful: Boolean) -> Unit)
    )

    suspend fun postSteps(
        timeRangeFilter: TimeRangeFilter,
        callback: ((error: String?, successful: Boolean) -> Unit)
    )

    suspend fun postHeartRates(
        timeRangeFilter: TimeRangeFilter,
        callback: ((error: String?, successful: Boolean) -> Unit)
    )
}