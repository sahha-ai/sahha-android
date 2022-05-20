package sdk.sahha.android.domain.use_case

import android.os.Build
import androidx.annotation.RequiresApi
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.repository.RemoteRepo
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class AnalyzeProfileUseCase @Inject constructor(
    private val repository: RemoteRepo,
    private val sahhaTimeManager: SahhaTimeManager?
) {
    suspend operator fun invoke(
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        repository.getAnalysis(callback = callback)
    }

    @JvmName("invokeDate")
    suspend operator fun invoke(
        dates: Pair<Date, Date>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        sahhaTimeManager?.also { timeManager ->
            val datesISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Pair(
                    timeManager.dateToISO(dates.first),
                    timeManager.dateToISO(dates.second)
                )
            } else {
                callback?.also {
                    it(
                        "Error: Android 7 or above required for specified dates",
                        null
                    )
                }
                return
            }

            repository.getAnalysis(datesISO, callback)
        }

        callback?.also { it(SahhaErrors.somethingWentWrong, null) }
    }

    @JvmName("invokeLocalDateTime")
    suspend operator fun invoke(
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        sahhaTimeManager?.also { timeManager ->
            val datesISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Pair(
                    timeManager.localDateTimeToISO(dates.first),
                    timeManager.localDateTimeToISO(dates.second)
                )
            } else {
                callback?.also { it("Error: Android 8 or above required for specified dates", null) }
                return
            }

            repository.getAnalysis(datesISO, callback)
        }

        callback?.also { it(SahhaErrors.somethingWentWrong, null) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(
        dates: Pair<Long, Long>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        sahhaTimeManager?.also { timeManager ->
            val datesISO = Pair(
                sahhaTimeManager.epochMillisToISO(dates.first),
                sahhaTimeManager.epochMillisToISO(dates.second)
            )

            repository.getAnalysis(datesISO, callback)
        }

        callback?.also { it(SahhaErrors.somethingWentWrong, null) }
    }
}