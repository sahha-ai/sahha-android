package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.RemoteRepo
import sdk.sahha.android.source.SahhaDemographic

class PostDemographicUseCase (
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, success: Boolean) -> Unit)?,
    ) {
        repository.postDemographic(sahhaDemographic, callback)
    }
}