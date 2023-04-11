package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.SahhaSensor
import javax.inject.Inject

class PostAllSensorDataUseCase @Inject constructor (
    private val repository: SensorRepo
) {
    suspend operator fun invoke(
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        repository.postAllSensorData(callback)
    }
}