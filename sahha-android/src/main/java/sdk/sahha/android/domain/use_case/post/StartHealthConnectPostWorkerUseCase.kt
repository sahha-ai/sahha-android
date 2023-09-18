package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.HealthConnectRepo
import javax.inject.Inject

class StartHealthConnectPostWorkerUseCase @Inject constructor(
    private val repository: HealthConnectRepo
) {
    operator fun invoke() {
        repository.startPostWorker()
    }
}