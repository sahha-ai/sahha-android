package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.BackgroundRepo
import sdk.sahha.android.source.SahhaSensor

class GetSensorDataUseCase(private val repository: BackgroundRepo) {
    suspend operator fun invoke(
        sensor: SahhaSensor,
        callback: ((error: String?, success: String?) -> Unit)
    ) {
        repository.getSensorData(
            sensor = sensor,
            callback = callback
        )
    }
}