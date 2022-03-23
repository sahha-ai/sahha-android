package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.BackgroundRepo
import javax.inject.Inject

class StartDataCollectionServiceUseCase @Inject constructor(
    private val repository: BackgroundRepo
) {
    operator fun invoke(icon: Int?, title: String?, shortDescription: String?) {
        repository.startDataCollectionService(icon, title, shortDescription)
    }
}