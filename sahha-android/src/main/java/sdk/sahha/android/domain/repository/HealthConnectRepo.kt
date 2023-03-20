package sdk.sahha.android.domain.repository

import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.source.HealthConnectSensor

interface HealthConnectRepo {
    suspend fun <T : Record> getSensorData(
        readRecordsRequest: ReadRecordsRequest<T>
    ): List<T>

    suspend fun getGrantedPermissions(): Set<Permission>
    suspend fun getLastPostTimestamp(sensorId: Int): Long?
    suspend fun saveLastPost(sensorId: Int, timestamp: Long)
    suspend fun postHealthConnectData(
        healthConnectSensor: Enum<HealthConnectSensor>,
        timeRangeFilter: TimeRangeFilter,
        callback: (error: String?, successful: Boolean) -> Unit
    )
}