package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.RemoteRepo
import sdk.sahha.android.source.SahhaSensor

class PostAllSensorDataUseCase (
    val repository: RemoteRepo
) {
    suspend operator fun invoke(
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        repository.postAllSensorData(callback)
    }
}