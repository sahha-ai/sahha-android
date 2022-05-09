package sdk.sahha.android.domain.use_case

import android.os.Build
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.repository.RemoteRepo
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject

class AnalyzeProfileUseCase @Inject constructor(
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(
        callback: ((error: String?, success: String?) -> Unit)?,
        startDate: String? = null,
        endDate: String? = null
    ) {
        repository.getAnalysis(callback, startDate, endDate)
    }
}