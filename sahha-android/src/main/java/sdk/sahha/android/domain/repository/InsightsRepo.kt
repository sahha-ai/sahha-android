package sdk.sahha.android.domain.repository

import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import sdk.sahha.android.domain.internal_enum.InsightPermission
import sdk.sahha.android.domain.model.insight.InsightData

interface InsightsRepo {
    fun getSleepStageSummary(sleepRecords: List<SleepSessionRecord>): HashMap<Int, Long>
    fun getMinutesSlept(
        sleepRecords: List<SleepSessionRecord>,
    ): Double

    fun getMinutesInBed(
        sleepRecords: List<SleepSessionRecord>,
    ): Double

    fun getMinutesInSleepStage(
        summary: HashMap<Int, Long>,
        sleepStage: Int,
    ): Long

    fun getStepCount(
        stepsRecords: List<StepsRecord>,
    ): Double

    suspend fun postInsights(
        token: String,
        insights: List<InsightData>,
        refreshedToken: Boolean = false,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)
    )

    suspend fun hasPermission(insightPermission: InsightPermission): Boolean
}