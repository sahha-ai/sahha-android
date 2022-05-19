package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.RemoteRepo
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class AnalyzeProfileUseCase @Inject constructor(
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(
        dates: Pair<Date, Date>?,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        repository.getAnalysis(dates, callback)
    }
}