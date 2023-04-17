package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.source.SahhaDemographic
import javax.inject.Inject

class PostDemographicUseCase @Inject constructor (
    private val repository: UserDataRepo
) {
    suspend operator fun invoke(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, success: Boolean) -> Unit)?,
    ) {
        repository.postDemographic(sahhaDemographic, callback)
    }
}