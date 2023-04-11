package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject

class StartPostWorkersUseCase @Inject constructor  (
    private val repository: SensorRepo
) {
    operator fun invoke() {
        repository.startPostWorkersAsync()
    }
}