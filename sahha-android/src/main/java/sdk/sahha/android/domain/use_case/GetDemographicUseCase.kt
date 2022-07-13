package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.RemoteRepo
import sdk.sahha.android.source.SahhaDemographic

class GetDemographicUseCase (
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(
        callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?
    ) {
        return repository.getDemographic(callback)
    }
}