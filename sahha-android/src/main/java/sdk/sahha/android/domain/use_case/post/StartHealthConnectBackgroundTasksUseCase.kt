package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.HealthConnectRepo
import javax.inject.Inject

internal class StartHealthConnectBackgroundTasksUseCase @Inject constructor(
    private val repository: HealthConnectRepo
) {
    operator fun invoke(
        callback: ((err: String?, success: Boolean) -> Unit)? = null
    ) {
        repository.startDevicePostWorker(callback)
    }
}