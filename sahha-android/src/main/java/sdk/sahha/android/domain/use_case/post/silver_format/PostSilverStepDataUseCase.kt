package sdk.sahha.android.domain.use_case.post.silver_format

import androidx.work.ListenableWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.Sahha
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.coroutines.resume

class PostSilverStepDataUseCase @Inject constructor(
    private val repository: SensorRepo,
    private val timeManager: SahhaTimeManager
) {
    internal var hourlySteps = listOf<StepSession>()

    suspend operator fun invoke(): ListenableWorker.Result {
        val truncatedStepData = truncateStepDataDates()
        hourlySteps = convertToHourly(truncatedStepData)
        return postSilverStepData(hourlySteps)
    }

    private fun convertToHourly(truncatedStepData: List<StepData>): List<StepSession> {
        val timeSeries = mutableMapOf<String, Int>()
        val hourlyStepSession = mutableListOf<StepSession>()

        for (it in truncatedStepData) {
            val stepDataIsInCurrentHour = it.detectedAt == timeManager.getCurrentHourIso(ZonedDateTime.now())
            if (stepDataIsInCurrentHour) continue // Skip until the hour of data is completed

            timeSeries[it.detectedAt] = timeSeries.getOrDefault(it.detectedAt, 0) + 1
        }

        timeSeries.forEach {
            hourlyStepSession.add(
                StepSession(
                    count = it.value,
                    startDateTime = it.key,
                    endDateTime = getEndOfTimeSeries(it.key),
                )
            )
        }
        return hourlyStepSession
    }

    private fun getEndOfTimeSeries(startTimeIso: String): String {
        val zdt = timeManager.ISOToDate(startTimeIso)
        val endTimeZdt = zdt.plusHours(1).minusNanos(1)
        return timeManager.zonedDateTimeToIso(endTimeZdt)
    }

    private suspend fun truncateStepDataDates(): List<StepData> {
        val singleStepData = getSingleStepData()
        return truncateSingleSteps(singleStepData)
    }

    private suspend fun getSingleStepData(): List<StepData> {
        return Sahha.di.sensorRepo.getAllSingleSteps()
    }

    private fun truncateSingleSteps(singleSteps: List<StepData>): List<StepData> {
        return singleSteps.map {
            val detectedIso = timeManager.ISOToDate(it.detectedAt)
            val truncatedHourlyZdt = detectedIso.truncatedTo(ChronoUnit.HOURS)
            val truncatedHourlyIso = timeManager.zonedDateTimeToIso(truncatedHourlyZdt)
            val truncatedHourlyData = StepData(
                Constants.STEP_DETECTOR_DATA_SOURCE,
                1,
                truncatedHourlyIso,
            )
            truncatedHourlyData
        }
    }

    private suspend fun postSilverStepData(silverStepData: List<StepSession>): ListenableWorker.Result {
        // Guard: Return and do nothing if there is no auth data
        if (Sahha.sim.auth.authIsInvalid(
                Sahha.di.authRepo.getToken(),
                Sahha.di.authRepo.getRefreshToken()
            )
        ) return ListenableWorker.Result.success()

        return if (Sahha.di.mutex.tryLock()) {
            try {
                suspendCancellableCoroutine<ListenableWorker.Result> { cont ->
                    Sahha.di.ioScope.launch {
                        repository.postStepsHourly(silverStepData) { _, _ ->
                            if (cont.isActive) {
                                cont.resume(ListenableWorker.Result.success())
                            }
                        }
                    }
                }
            } finally {
                Sahha.di.mutex.unlock()
            }
        } else {
            ListenableWorker.Result.retry()
        }
    }
}