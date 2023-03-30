package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.SensorRepo

class StartPostWorkersUseCase (
    private val repository: SensorRepo
) {
    operator fun invoke() {
        repository.startPostWorkersAsync()
    }
}