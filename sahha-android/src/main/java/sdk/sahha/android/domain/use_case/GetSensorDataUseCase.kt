package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.SahhaSensor

class GetSensorDataUseCase(private val repository: SensorRepo) {
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