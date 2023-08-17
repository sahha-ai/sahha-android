package sdk.sahha.android.domain.use_case.post.silver_format

import androidx.work.ListenableWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.device.PhoneUsageHourly
import sdk.sahha.android.domain.model.device.PhoneUsageSilver
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.interaction.AuthInteractionManager
import sdk.sahha.android.source.Sahha
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.coroutines.resume

class PostSilverDeviceDataUseCase @Inject constructor(
    private val repository: SensorRepo,
    private val auth: AuthInteractionManager,
    private val timeManager: SahhaTimeManager
) {
    internal var phoneUsagesSilver = listOf<PhoneUsageSilver>()

    suspend operator fun invoke(): ListenableWorker.Result {
        val phoneUsagesHourly = getPreparedData()
        return postSilverPhoneUsageData(phoneUsagesHourly)
    }

    internal suspend fun getPreparedData(): List<PhoneUsageHourly> {
        val truncatedUsages = truncatePhoneUsages()
        return summariseTruncatedData(truncatedUsages)
    }

    private suspend fun truncatePhoneUsages(): List<PhoneUsageSilver> {
        val usagesSilver = repository.getAllPhoneUsagesSilver()
        phoneUsagesSilver = usagesSilver

        return usagesSilver.map {
            val isoToDate = timeManager.ISOToDate(it.detectedAt)
            val truncatedHourly = isoToDate.truncatedTo(ChronoUnit.HOURS)
            val truncatedHourlyToIso = timeManager.zonedDateTimeToIso(truncatedHourly)

            return@map PhoneUsageSilver(
                it.isLocked, it.isScreenOn, truncatedHourlyToIso
            )
        }
    }

    private fun summariseTruncatedData(truncatedUsages: List<PhoneUsageSilver>): List<PhoneUsageHourly> {
        val timeSeriesLocked = mutableMapOf<String, Int>()
        val timeSeriesUnlocked = mutableMapOf<String, Int>()
        val timeSeriesScreenOn = mutableMapOf<String, Int>()
        val timeSeriesScreenOff = mutableMapOf<String, Int>()

        for (usage in truncatedUsages) {
            val isCurrentHour =
                usage.detectedAt == timeManager.getCurrentHourIso(ZonedDateTime.now())
            if (isCurrentHour) continue

            timeSeriesLocked[usage.detectedAt] =
                incrementOnLocked(usage, timeSeriesLocked.getOrDefault(usage.detectedAt, 0))
            timeSeriesUnlocked[usage.detectedAt] =
                incrementOnUnlocked(usage, timeSeriesUnlocked.getOrDefault(usage.detectedAt, 0))
            timeSeriesScreenOn[usage.detectedAt] =
                incrementOnScreenOn(usage, timeSeriesScreenOn.getOrDefault(usage.detectedAt, 0))
            timeSeriesScreenOff[usage.detectedAt] =
                incrementOnScreenOff(usage, timeSeriesScreenOff.getOrDefault(usage.detectedAt, 0))
        }

        val phoneUsagesHourly = mutableListOf<PhoneUsageHourly>()

        timeSeriesLocked.forEach {
            phoneUsagesHourly.add(
                PhoneUsageHourly(
                    lockCount = timeSeriesLocked.getOrDefault(it.key, 0),
                    unlockCount = timeSeriesUnlocked.getOrDefault(it.key, 0),
                    screenOnCount = timeSeriesScreenOn.getOrDefault(it.key, 0),
                    screenOffCount = timeSeriesScreenOff.getOrDefault(it.key, 0),
                    start = it.key,
                    end = timeManager.isoTimePlusHours(it.key, 1)
                )
            )
        }

        return phoneUsagesHourly
    }

    private fun incrementOnLocked(phoneUsageSilver: PhoneUsageSilver, lockedCount: Int): Int {
        return if (phoneUsageSilver.isLocked) lockedCount + 1 else lockedCount
    }

    private fun incrementOnUnlocked(phoneUsageSilver: PhoneUsageSilver, unlockedCount: Int): Int {
        return if (phoneUsageSilver.isLocked) unlockedCount else unlockedCount + 1
    }

    private fun incrementOnScreenOn(phoneUsageSilver: PhoneUsageSilver, screenOnCount: Int): Int {
        return if (phoneUsageSilver.isScreenOn) screenOnCount + 1 else screenOnCount
    }

    private fun incrementOnScreenOff(phoneUsageSilver: PhoneUsageSilver, screenOffCount: Int): Int {
        return if (phoneUsageSilver.isScreenOn) screenOffCount else screenOffCount + 1
    }

    private suspend fun postSilverPhoneUsageData(hourlyPhoneUsages: List<PhoneUsageHourly>): ListenableWorker.Result {
        // Guard: Return and do nothing if there is no auth data
        if (!auth.checkIsAuthenticated()) return ListenableWorker.Result.success()

        return if (Sahha.di.mutex.tryLock()) {
            try {
                suspendCancellableCoroutine<ListenableWorker.Result> { cont ->
                    Sahha.di.ioScope.launch {
                        repository.postPhoneUsagesHourly(hourlyPhoneUsages) { _, success ->
                            if (success) repository.clearPhoneUsagesSilver(phoneUsagesSilver)

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