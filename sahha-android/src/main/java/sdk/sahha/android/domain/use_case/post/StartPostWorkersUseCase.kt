package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.domain.repository.BackgroundRepo

class StartPostWorkersUseCase (
    private val repository: BackgroundRepo
) {
    operator fun invoke() {
        repository.startPostWorkersAsync()
    }
}