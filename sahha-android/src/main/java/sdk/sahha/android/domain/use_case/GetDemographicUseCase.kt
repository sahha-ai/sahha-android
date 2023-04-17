package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.source.SahhaDemographic
import javax.inject.Inject

class GetDemographicUseCase @Inject constructor (
    private val repository: UserDataRepo
) {
    suspend operator fun invoke(
        callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?
    ) {
        return repository.getDemographic(callback)
    }
}