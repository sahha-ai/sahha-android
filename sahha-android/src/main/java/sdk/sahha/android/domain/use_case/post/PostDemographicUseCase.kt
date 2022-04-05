package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.model.profile.SahhaDemographic
import sdk.sahha.android.domain.repository.RemoteRepo
import javax.inject.Inject

class PostDemographicUseCase @Inject constructor(
    private val repository: RemoteRepo
) {
    suspend operator fun invoke(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        repository.postDemographic(sahhaDemographic, callback)
    }
}