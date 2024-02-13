package sdk.sahha.android.domain.interaction

import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
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
import sdk.sahha.android.data.mapper.toActiveEnergyInsight
import sdk.sahha.android.data.mapper.toTotalEnergyInsight
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.internal_enum.InsightPermission
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.InsightsRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.source.SahhaSensor
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class InsightsInteractionManager @Inject constructor(
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
        val postJob = ioScope.launch {
            try {
                suspendCoroutine { cont ->
                    ioScope.launch {
                        withTimeout(Constants.POST_TIMEOUT_LIMIT_MILLIS) {
                            postInsightsData { _, _ ->
                                cont.resume(Unit)
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                this.cancel(e)
            }
        }
        val minimumDelayJob = ioScope.launch { delay(delayMilliseconds) }

        val jobs = listOf(postJob, minimumDelayJob)
        jobs.joinAll()
    }

    private suspend fun postInsightsData(callback: (suspend (error: String?, successful: Boolean) -> Unit)) {
        val token = authRepo.getToken() ?: ""
        val sensors = configRepo.getConfig().sensorArray
        insights.clear()

        val now = ZonedDateTime.now()

        if (sensors.contains(SahhaSensor.sleep.ordinal)) {
            checkAndAddSleepInsights(
                LocalDateTime.of(
                    now.minusDays(1).toLocalDate(),
                    LocalTime.of(Constants.ALARM_6PM, 0)
                ),
                LocalDateTime.of(
                    now.toLocalDate(),
                    LocalTime.of(Constants.ALARM_6PM, 0)
                )
            )
        }

        if (sensors.contains(SahhaSensor.activity.ordinal)) {
            checkAndAddStepsInsight(
                LocalDateTime.of(
                    now.minusDays(1).toLocalDate(),
                    LocalTime.of(Constants.ALARM_12AM, 0)
                ),
                LocalDateTime.of(
                    now.minusDays(1).toLocalDate(),
                    LocalTime.of(23, 59, 59, 99)
                )
            )
        }

        if (sensors.contains(SahhaSensor.energy.ordinal)) {
            checkAndAddEnergyInsights(
                LocalDateTime.of(
                    now.minusDays(1).toLocalDate(),
                    LocalTime.of(Constants.ALARM_12AM, 0)
                ),
                LocalDateTime.of(
                    now.toLocalDate(),
                    LocalTime.of(Constants.ALARM_12AM, 0)
                ),
            )
        }

        insights.ifEmpty {
            callback(SahhaErrors.noInsightsData, false)
            return
        }
        insightsRepo.postInsights(token = token, insights = insights, callback = callback)
    }

    private suspend fun checkAndAddSleepInsights(start: LocalDateTime, end: LocalDateTime) {
        if (!insightsRepo.hasPermission(InsightPermission.sleep)) return

        val sleepRecords = healthConnectRepo.getRecords(
            SleepSessionRecord::class,
            TimeRangeFilter.between(start, end),
        )

        sleepRecords?.also { records ->
            val summary = insightsRepo.getSleepStageSummary(records)

            insights.add(
                InsightData(
                    Constants.INSIGHT_NAME_TIME_ASLEEP,
                    insightsRepo.getMinutesSlept(records),
                    Constants.DataUnits.MINUTE,
                    timeManager.localDateTimeToISO(start),
                    timeManager.localDateTimeToISO(end)
                )
            )
            insights.add(
                InsightData(
                    Constants.INSIGHT_NAME_TIME_IN_BED,
                    insightsRepo.getMinutesInBed(records),
                    Constants.DataUnits.MINUTE,
                    timeManager.localDateTimeToISO(start),
                    timeManager.localDateTimeToISO(end)
                )
            )
            insights.add(
                InsightData(
                    Constants.INSIGHT_NAME_TIME_IN_REM_SLEEP,
                    insightsRepo.getMinutesInSleepStage(summary, SleepSessionRecord.STAGE_TYPE_REM),
                    Constants.DataUnits.MINUTE,
                    timeManager.localDateTimeToISO(start),
                    timeManager.localDateTimeToISO(end)
                )
            )
            insights.add(
                InsightData(
                    Constants.INSIGHT_NAME_TIME_IN_LIGHT_SLEEP,
                    insightsRepo.getMinutesInSleepStage(
                        summary,
                        SleepSessionRecord.STAGE_TYPE_LIGHT
                    ),
                    Constants.DataUnits.MINUTE,
                    timeManager.localDateTimeToISO(start),
                    timeManager.localDateTimeToISO(end)
                )
            )
            insights.add(
                InsightData(
                    Constants.INSIGHT_NAME_TIME_IN_DEEP_SLEEP,
                    insightsRepo.getMinutesInSleepStage(
                        summary,
                        SleepSessionRecord.STAGE_TYPE_DEEP
                    ),
                    Constants.DataUnits.MINUTE,
                    timeManager.localDateTimeToISO(start),
                    timeManager.localDateTimeToISO(end)
                )
            )
        }
    }

    private suspend fun checkAndAddStepsInsight(start: LocalDateTime, end: LocalDateTime) {
        if (!insightsRepo.hasPermission(InsightPermission.steps)) return

        val stepsRecords = healthConnectRepo.getRecords(
            StepsRecord::class,
            TimeRangeFilter.between(start, end),
        )

        stepsRecords?.also { records ->
            insights.add(
                InsightData(
                    Constants.INSIGHT_NAME_STEP_COUNT,
                    insightsRepo.getStepCount(records),
                    Constants.DataUnits.COUNT,
                    timeManager.localDateTimeToISO(start),
                    timeManager.localDateTimeToISO(end)
                )
            )
        }
    }

    private suspend fun checkAndAddEnergyInsights(
        start: LocalDateTime,
        end: LocalDateTime,
        zoneOffset: ZoneOffset = ZonedDateTime.now().offset,
        duration: Duration = Duration.ofDays(1)
    ) {
        if (!insightsRepo.hasPermission(InsightPermission.active_energy)) return
        if (!insightsRepo.hasPermission(InsightPermission.total_energy)) return

        val activeEnergy = healthConnectRepo.getAggregateRecordsByDuration(
            metrics = setOf(
                ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
            ),
            TimeRangeFilter.between(start.toInstant(zoneOffset), end.toInstant(zoneOffset)),
            duration
        )

        val totalEnergy = healthConnectRepo.getAggregateRecordsByDuration(
            metrics = setOf(
                TotalCaloriesBurnedRecord.ENERGY_TOTAL,
            ),
            TimeRangeFilter.between(start.toInstant(zoneOffset), end.toInstant(zoneOffset)),
            duration
        )

        activeEnergy?.forEach { record ->
            insights.add(record.toActiveEnergyInsight())
        }

        totalEnergy?.forEach { record ->
            insights.add(record.toTotalEnergyInsight())
        }
    }
}