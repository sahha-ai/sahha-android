package sdk.sahha.android.data.repository

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.Sahha

class HealthConnectRepoImpl(
    private val healthConnectClient: HealthConnectClient,
) : HealthConnectRepo {
    private val timeManager by lazy { Sahha.di.timeManager }
    private val configurationDao by lazy { Sahha.di.configurationDao }
    private val nowInEpochMillis = timeManager.nowInEpoch()

    private suspend fun getCurrentTimeRangeFilter(): TimeRangeFilter {
        return timeManager.getTimeRangeFilter(
            configurationDao.getLastHealthConnectPost()?.epochMillis
                ?: getEpochMillisFrom(7),
            nowInEpochMillis
        )
    }

    private fun getEpochMillisFrom(days: Int): Long {
        return nowInEpochMillis - (Constants.ONE_DAY_IN_MILLIS * days)
    }

    override suspend fun getStepData(): List<StepsRecord> {
        return healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = getCurrentTimeRangeFilter(),
            )
        ).records
    }

    override suspend fun getSleepData(): List<SleepSessionRecord> {
        return healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = getCurrentTimeRangeFilter()
            )
        ).records
    }

    override suspend fun getSleepStageData(): List<SleepStageRecord> {
        return healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepStageRecord::class,
                timeRangeFilter = getCurrentTimeRangeFilter()
            )
        ).records
    }

    override suspend fun getHeartRateData(): List<HeartRateRecord> {
        return healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = getCurrentTimeRangeFilter()
            )
        ).records
    }
}