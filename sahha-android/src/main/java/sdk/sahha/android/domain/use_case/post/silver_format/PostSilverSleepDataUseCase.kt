package sdk.sahha.android.domain.use_case.post.silver_format

import androidx.work.ListenableWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.dto.send.SleepSendDto
import sdk.sahha.android.domain.model.sleep.SleepDataSilver
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.interaction.AuthInteractionManager
import sdk.sahha.android.source.Sahha
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.coroutines.resume

class PostSilverSleepDataUseCase @Inject constructor(
    private val repository: SensorRepo,
    private val auth: AuthInteractionManager,
    private val timeManager: SahhaTimeManager
) {
    internal var sleepDataHourly = mutableListOf<SleepSendDto>()

    suspend operator fun invoke(): ListenableWorker.Result {
        val sleepHourly = convertSleepDataHourly()
        return postHourlySleepData(sleepHourly)
    }

    private suspend fun convertSleepDataHourly(): List<SleepSendDto> {
        val sleepData = repository.getAllSleepSilver()
        val convertedSleepData = mutableListOf<SleepSendDto>()

        for (sleep in sleepData) {
            convertedSleepData += segregateHourly(sleep)
        }

        return convertedSleepData
    }

    private fun segregateHourly(sleep: SleepDataSilver): List<SleepSendDto> {
        val startAsDate = timeManager.ISOToDate(sleep.startDateTime)
        val endAsDate = timeManager.ISOToDate(sleep.endDateTime)

        val truncatedHours = getTruncatedHours(startAsDate, endAsDate)

        val minutesPerSegment = mutableListOf<SleepSendDto>()

        // Start at 2nd index and subtract previous index
        for (i in 1 until truncatedHours.count()) {
            minutesPerSegment.add(
                SleepSendDto(
                    source = sleep.source,
                    sleepStage = sleep.sleepStage,
                    durationInMinutes = getDurationInMinutes(
                        truncatedHours[i - 1],
                        truncatedHours[i]
                    ),
                    startDateTime = timeManager.zonedDateTimeToIso(
                        truncatedHours[i - 1].truncatedTo(ChronoUnit.HOURS)
                    ),
                    endDateTime = timeManager.zonedDateTimeToIso(
                        truncatedHours[i].truncatedTo(ChronoUnit.HOURS)
                    )
                )
            )
        }

        return minutesPerSegment
    }

    private fun getDurationInMinutes(start: ZonedDateTime, end: ZonedDateTime): Int {
        val startEpochMillis = start.toInstant().toEpochMilli()
        val endEpochMillis = end.toInstant().toEpochMilli()
        return timeManager.millisecondsToMinutes(endEpochMillis - startEpochMillis).toInt()
    }

    private fun getTruncatedHours(
        startAsDate: ZonedDateTime,
        endAsDate: ZonedDateTime
    ): List<ZonedDateTime> {
        val endTruncated = endAsDate.truncatedTo(ChronoUnit.HOURS)

        var currentHour = startAsDate
        val hourlyTruncated = mutableListOf<ZonedDateTime>()

        while (currentHour <= endTruncated) {
            if (currentHour == endTruncated) {
                hourlyTruncated.add(endAsDate)
                break
            }

            hourlyTruncated.add(currentHour)
            currentHour = incrementHourTruncated(currentHour)
        }

        return hourlyTruncated
    }

    private fun incrementHourTruncated(currentHour: ZonedDateTime): ZonedDateTime {
        return currentHour.truncatedTo(ChronoUnit.HOURS).plusHours(1)
    }

    private suspend fun postHourlySleepData(sleepHourly: List<SleepSendDto>): ListenableWorker.Result {
        // Guard: Return and do nothing if there is no auth data
        if (!auth.checkIsAuthenticated()) return ListenableWorker.Result.success()

        return if (Sahha.di.mutex.tryLock()) {
            try {
                suspendCancellableCoroutine<ListenableWorker.Result> { cont ->
                    Sahha.di.ioScope.launch {
                        /* TODO: PostSleepHourly { _, _ ->
                            if (cont.isActive) {
                                cont.resume(ListenableWorker.Result.success())
                            }
                        } */
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