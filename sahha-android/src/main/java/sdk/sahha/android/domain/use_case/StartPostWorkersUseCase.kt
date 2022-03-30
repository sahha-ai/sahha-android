package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.BackgroundRepo
import javax.inject.Inject

class StartPostWorkersUseCase @Inject constructor(
    private val repository: BackgroundRepo
) {
    operator fun invoke() {
        repository.startPostWorkersAsync()
    }
}