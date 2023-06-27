package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.HealthConnectRepo
import javax.inject.Inject

class PostHealthConnectDataUseCase @Inject constructor(
    private val repo: HealthConnectRepo
) {
    operator fun invoke(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {

    }
}