package sdk.sahha.android.data.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import sdk.sahha.android.domain.repository.HealthConnectRepo

class HealthConnectRepoImpl(
    private val healthConnectClient: HealthConnectClient,
    ): HealthConnectRepo {
    override suspend fun getStepData(context: Context): List<StepsRecord> {
        TODO("Not yet implemented")
    }

    override suspend fun getSleepData(): List<SleepSessionRecord> {
        TODO("Not yet implemented")
    }

    override suspend fun getSleepStageData(): List<SleepStageRecord> {
        TODO("Not yet implemented")
    }

    override suspend fun getHeartRateData(): List<HeartRateRecord> {
        TODO("Not yet implemented")
    }
}