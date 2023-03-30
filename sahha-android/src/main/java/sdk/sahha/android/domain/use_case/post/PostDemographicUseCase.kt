package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.source.SahhaDemographic

class PostDemographicUseCase(
    private val repository: UserDataRepo
) {
    suspend operator fun invoke(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, success: Boolean) -> Unit)?,
    ) {
        repository.postDemographic(sahhaDemographic, callback)
    }
}