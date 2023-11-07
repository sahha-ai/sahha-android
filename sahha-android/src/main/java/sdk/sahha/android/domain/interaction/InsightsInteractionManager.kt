package sdk.sahha.android.domain.interaction

import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.InsightsRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.source.SahhaSensor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InsightsInteractionManager @Inject constructor(
    private val authRepo: AuthRepo,
    private val insightsRepo: InsightsRepo,
    private val healthConnectRepo: HealthConnectRepo,
    private val configRepo: SahhaConfigRepo,
    private val timeManager: SahhaTimeManager,
    @IoScope private val ioScope: CoroutineScope,
) {
    private val insights = mutableListOf<InsightData>()

    suspend fun postWithMinimumDelay(
        delayMilliseconds: Long = Constants.TEMP_FOREGROUND_NOTIFICATION_DURATION_MILLIS,
    ) {
        println("InsightsInteractionManager0003")
        val postJob = ioScope.launch {
            println("InsightsInteractionManager0004")
            try {
                println("InsightsInteractionManager0005")
                suspendCoroutine { cont ->
                    println("InsightsInteractionManager0006")
                    ioScope.launch {
                        println("InsightsInteractionManager0007")
                        withTimeout(Constants.POST_TIMEOUT_LIMIT_MILLIS) {
                            postInsightsData { _, _ ->
                                println("InsightsInteractionManager0011")
                                cont.resume(Unit)
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                println("InsightsInteractionManager0008")
                this.cancel(e)
            }
        }
        val minimumDelayJob = ioScope.launch { delay(delayMilliseconds) }

        println("InsightsInteractionManager0009")
        val jobs = listOf(postJob, minimumDelayJob)
        jobs.joinAll()
        println("InsightsInteractionManager0010")
    }

    suspend fun postInsightsData(callback: (suspend (error: String?, successful: Boolean) -> Unit)) {
        println("InsightsInteractionManager0020")
        val token = authRepo.getToken() ?: ""
        val sensors = configRepo.getConfig().sensorArray
        insights.clear()

        if (sensors.contains(SahhaSensor.sleep.ordinal)) {
            println("InsightsInteractionManager0021")
            addSleepInsights(
                LocalDateTime.of(
                    LocalDate.now().minusDays(1),
                    LocalTime.of(Constants.INSIGHTS_SLEEP_ALARM_HOUR, 0)
                ),
                LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(Constants.INSIGHTS_SLEEP_ALARM_HOUR, 0)
                )
            )
        }

        if (sensors.contains(SahhaSensor.pedometer.ordinal)) {
            println("InsightsInteractionManager0022")
            addStepsInsight(
                LocalDateTime.of(
                    LocalDate.now().minusDays(1),
                    LocalTime.of(Constants.INSIGHTS_STEPS_ALARM_HOUR, 0)
                ),
                LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.of(Constants.INSIGHTS_STEPS_ALARM_HOUR, 0)
                )
            )
        }

        insights.ifEmpty {
            println("InsightsInteractionManager0023")
            callback(SahhaErrors.noInsightsData, false)
            return
        }
        println("InsightsInteractionManager0024")
        insightsRepo.postInsights(token = token, insights = insights, callback = callback)
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