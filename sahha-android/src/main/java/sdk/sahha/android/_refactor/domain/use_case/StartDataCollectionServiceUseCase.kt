package sdk.sahha.android._refactor.domain.use_case

import sdk.sahha.android._refactor.domain.repository.BackgroundRepo
import javax.inject.Inject

internal class StartDataCollectionServiceUseCase @Inject constructor(
    private val repository: BackgroundRepo
) {
    suspend operator fun invoke() {
        repository.startDataCollectionService()
    }
}