package sdk.sahha.android.domain.repository

import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord

interface HealthConnectRepo {
    suspend fun getStepData(): List<StepsRecord>
    suspend fun getSleepData(): List<SleepSessionRecord>
    suspend fun getSleepStageData(): List<SleepStageRecord>
    suspend fun getHeartRateData(): List<HeartRateRecord>
}