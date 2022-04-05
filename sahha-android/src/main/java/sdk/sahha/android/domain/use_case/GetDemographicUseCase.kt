package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.model.profile.SahhaDemographic
import sdk.sahha.android.domain.repository.RemoteRepo
import javax.inject.Inject

class GetDemographicUseCase @Inject constructor(
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(
        callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?
    ) {
        return repository.getDemographic(callback)
    }
}