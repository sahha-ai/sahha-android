package sdk.sahha.android.domain.repository

import android.content.Context
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord

interface HealthConnectRepo {
    suspend fun getStepData(context: Context): List<StepsRecord>
    suspend fun getSleepData(): List<SleepSessionRecord>
    suspend fun getSleepStageData(): List<SleepStageRecord>
    suspend fun getHeartRateData(): List<HeartRateRecord>
}