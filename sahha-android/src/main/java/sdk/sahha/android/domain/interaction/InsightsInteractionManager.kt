package sdk.sahha.android.domain.interaction

import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.InsightsRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.source.SahhaSensor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class InsightsInteractionManager @Inject constructor(
    private val insightsRepo: InsightsRepo,
    private val healthConnectRepo: HealthConnectRepo,
    private val configRepo: SahhaConfigRepo,
    private val timeManager: SahhaTimeManager
) {
    private val insights = mutableListOf<InsightData>()
    suspend fun postInsightsData(callback: ((error: String?, successful: Boolean) -> Unit)) {
        val sensors = configRepo.getConfig().sensorArray
        insights.clear()

        if (sensors.contains(SahhaSensor.sleep.ordinal)) {
            addSleepInsights(
                LocalDateTime.of(
                    LocalDate.now().minusDays(1),
                    LocalTime.of(18, 0)
                ),
                LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(18, 0)
                )
            )
        }

        if (sensors.contains(SahhaSensor.pedometer.ordinal)) {
            addStepsInsight(
                LocalDateTime.of(
                    LocalDate.now().minusDays(1),
                    LocalTime.of(0, 0)
                ),
                LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(0, 0)
                )
            )
        }

        insights.ifEmpty { return }
        insightsRepo.postInsights(insights, callback)
    }

    private suspend fun addSleepInsights(start: LocalDateTime, end: LocalDateTime) {
        val sleepRecords = healthConnectRepo.getRecords(
            SleepSessionRecord::class,
            TimeRangeFilter.between(start, end),
        )

        sleepRecords?.also { records ->
            insights.add(
                InsightData(
                    Constants.INSIGHT_NAME_TIME_ASLEEP,
                    insightsRepo.getMinutesSlept(records),
                    Constants.UNIT_MINUTES,
                    timeManager.localDateTimeToISO(start),
                    timeManager.localDateTimeToISO(end)
                )
            )
            insights.add(
                InsightData(
                    Constants.INSIGHT_NAME_TIME_IN_BED,
                    insightsRepo.getMinutesInBed(records),
                    Constants.UNIT_MINUTES,
                    timeManager.localDateTimeToISO(start),
                    timeManager.localDateTimeToISO(end)
                )
            )
        }
    }

    private suspend fun addStepsInsight(start: LocalDateTime, end: LocalDateTime) {
        val stepsRecords = healthConnectRepo.getRecords(
            StepsRecord::class,
            TimeRangeFilter.between(start, end),
        )

        stepsRecords?.also { records ->
            insights.add(
                InsightData(
                    Constants.INSIGHT_NAME_STEP_COUNT,
                    insightsRepo.getStepCount(records),
                    Constants.UNIT_STEPS,
                    timeManager.localDateTimeToISO(start),
                    timeManager.localDateTimeToISO(end)
                )
            )
        }
    }
}