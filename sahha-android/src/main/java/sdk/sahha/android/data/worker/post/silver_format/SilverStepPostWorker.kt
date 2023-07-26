package sdk.sahha.android.data.worker.post.silver_format

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.source.Sahha
import java.time.temporal.ChronoUnit
import kotlin.coroutines.resume

class SilverStepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    internal var hourlySteps = listOf<StepSession>()
    override suspend fun doWork(): Result {
        SahhaReconfigure(context)

        val truncatedStepData = truncateStepDataDates()
        hourlySteps = converToHourly(truncatedStepData)
        return postSilverStepData(hourlySteps)
    }

    private fun converToHourly(truncatedStepData: List<StepData>): List<StepSession> {
        val timeSeries = mutableMapOf<String, Int>()
        val hourlyStepSession = mutableListOf<StepSession>()
        truncatedStepData.forEach {
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
        val zdt = Sahha.di.timeManager.ISOToDate(startTimeIso)
        val endTimeZdt = zdt.plusHours(1).minusNanos(1)
        return Sahha.di.timeManager.zonedDateTimeToIso(endTimeZdt)
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
            val detectedIso = Sahha.di.timeManager.ISOToDate(it.detectedAt)
            val truncatedHourlyZdt = detectedIso.truncatedTo(ChronoUnit.HOURS)
            val truncatedHourlyIso = Sahha.di.timeManager.zonedDateTimeToIso(truncatedHourlyZdt)
            val truncatedHourlyData = StepData(
                Constants.STEP_DETECTOR_DATA_SOURCE,
                1,
                truncatedHourlyIso,
            )
            truncatedHourlyData
        }
    }

    private suspend fun postSilverStepData(silverStepData: List<StepSession>): Result {
        // Guard: Return and do nothing if there is no auth data
        if (Sahha.sim.auth.authIsInvalid(
                Sahha.di.authRepo.getToken(),
                Sahha.di.authRepo.getRefreshToken()
            )
        ) return Result.success()

        return if (Sahha.di.mutex.tryLock()) {
            try {
                suspendCancellableCoroutine<Result> { cont ->
                    Sahha.di.ioScope.launch {
                        Sahha.sim.sensor.postStepsHourly(silverStepData) { _, _ ->
                            if (cont.isActive) {
                                cont.resume(Result.success())
                            }
                        }
                    }
                }
            } finally {
                Sahha.di.mutex.unlock()
            }
        } else {
            Result.retry()
        }
    }
}