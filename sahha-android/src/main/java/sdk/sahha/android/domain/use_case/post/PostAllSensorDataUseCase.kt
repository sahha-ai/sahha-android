package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.RemoteRepo
import sdk.sahha.android.source.SahhaSensor
import javax.inject.Inject

class PostAllSensorDataUseCase @Inject constructor(
    val repository: RemoteRepo
) {
    suspend operator fun invoke(
        sensors: Set<Enum<SahhaSensor>>?,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        repository.postAllSensorData(sensors, callback)
    }
}