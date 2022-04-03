package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.RemoteRepo
import javax.inject.Inject

class AnalyzeProfileUseCase @Inject constructor(
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(callback: ((error: String?, success: String?) -> Unit)?) {
        repository.getAnalysis(callback)
    }
}