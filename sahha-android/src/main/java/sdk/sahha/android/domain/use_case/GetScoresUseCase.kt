package sdk.sahha.android.domain.use_case

import android.os.Build
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.source.SahhaScoreType
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject

private const val TAG = "GetScoresUseCase"

internal class GetScoresUseCase @Inject constructor(
    private val repository: UserDataRepo,
    private val sahhaTimeManager: SahhaTimeManager?,
    private val sahhaErrorLogger: SahhaErrorLogger? = null
) {
    suspend operator fun invoke(
        types: Set<SahhaScoreType>,
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        repository.getScores(scoresString = types.map { it.name }, callback = callback)
    }

    @JvmName("invokeDate")
    suspend operator fun invoke(
        types: Set<SahhaScoreType>,
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

                repository.getScores(types.map { it.name }, datesISO, callback)
            } ?: callback?.also {
                it(SahhaErrors.androidVersionTooLow(7), null)

                sahhaErrorLogger?.application(
                    SahhaErrors.androidVersionTooLow(7),
                    TAG,
                    dates.toString()
                )
            }

        } catch (e: Exception) {
            callback?.also { it("Error: ${e.message}", null) }

            sahhaErrorLogger?.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                TAG,
                "invoke",
                dates.toString()
            )
        }
    }

    @JvmName("invokeLocalDateTime")
    suspend operator fun invoke(
        types: Set<SahhaScoreType>,
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

                repository.getScores(types.map { it.name }, datesISO, callback)
            } ?: callback?.also {
                it(SahhaErrors.androidVersionTooLow(8), null)
                sahhaErrorLogger?.application(
                    SahhaErrors.androidVersionTooLow(8),
                    TAG,
                    dates.toString()
                )
            }
        } catch (e: Exception) {
            callback?.also { it("Error: ${e.message}", null) }
            sahhaErrorLogger?.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                TAG,
                "invoke",
                dates.toString()
            )
        }
    }
}