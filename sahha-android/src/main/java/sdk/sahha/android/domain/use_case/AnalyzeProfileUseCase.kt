package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.RemoteRepo
import java.time.LocalDateTime
import javax.inject.Inject

class AnalyzeProfileUseCase @Inject constructor(
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(
        callback: ((error: String?, success: String?) -> Unit)?,
        dates: Pair<LocalDateTime, LocalDateTime>?
    ) {
        repository.getAnalysis(callback, dates)
    }
}