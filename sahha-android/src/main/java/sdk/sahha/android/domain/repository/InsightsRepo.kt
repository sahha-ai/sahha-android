package sdk.sahha.android.domain.repository

import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.domain.model.insight.InsightData

interface InsightsRepo {
    fun getMinutesSlept(
        sleepRecords: List<SleepSessionRecord>,
    ): Long

    fun getMinutesInBed(
        sleepRecords: List<SleepSessionRecord>,
    ): Long

    fun getStepCount(
        stepsRecords: List<StepsRecord>,
    ): Long

    suspend fun postInsights(
        insights: List<InsightData>,
        callback: ((error: String?, successful: Boolean) -> Unit)
    ): Response<ResponseBody>
}