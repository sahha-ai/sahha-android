package sdk.sahha.android.domain.use_case

import android.os.Build
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.repository.UserDataRepo
import java.time.LocalDateTime
import java.util.*

class AnalyzeProfileUseCase (
    private val repository: UserDataRepo,
    private val sahhaTimeManager: SahhaTimeManager?,
    private val sahhaErrorLogger: SahhaErrorLogger? = null
) {
    suspend operator fun invoke(
        includeSourceData: Boolean,
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        repository.getAnalysis(includeSourceData = includeSourceData, callback = callback)
    }

    @JvmName("invokeDate")
    suspend operator fun invoke(
        includeSourceData: Boolean,
        dates: Pair<Date, Date>,
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        try {
            sahhaTimeManager?.also { timeManager ->
                val datesISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Pair(
                        timeManager.dateToISO(dates.first),
                        timeManager.dateToISO(dates.second)
                    )
                } else {
                    callback?.also {
                        it(
                            SahhaErrors.androidVersionTooLow(7),
                            null
                        )
                    }
                    return
                }

                repository.getAnalysis(datesISO, includeSourceData, callback)
            } ?: callback?.also {
                it(SahhaErrors.androidVersionTooLow(7), null)

                sahhaErrorLogger?.application(
                    SahhaErrors.androidVersionTooLow(7),
                    "AnalyzeProfileUseCase",
                    dates.toString()
                )
            }

        } catch (e: Exception) {
            callback?.also { it("Error: ${e.message}", null) }

            sahhaErrorLogger?.application(
                e.message,
                "AnalyzeProfileUseCase",
                dates.toString()
            )
        }
    }

    @JvmName("invokeLocalDateTime")
    suspend operator fun invoke(
        includeSourceData: Boolean,
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        try {
            sahhaTimeManager?.also { timeManager ->
                val datesISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Pair(
                        timeManager.localDateTimeToISO(dates.first),
                        timeManager.localDateTimeToISO(dates.second)
                    )
                } else {
                    callback?.also {
                        it(
                            SahhaErrors.androidVersionTooLow(8),
                            null
                        )
                    }
                    return
                }

                repository.getAnalysis(datesISO, includeSourceData, callback)
            } ?: callback?.also {
                it(SahhaErrors.androidVersionTooLow(8), null)
                sahhaErrorLogger?.application(
                    SahhaErrors.androidVersionTooLow(8),
                    "AnalyzeProfileUseCase",
                    dates.toString()
                )
            }
        } catch (e: Exception) {
            callback?.also { it("Error: ${e.message}", null) }
            sahhaErrorLogger?.application(
                e.message,
                "AnalyzeProfileUseCase",
                dates.toString()
            )
        }
    }
}