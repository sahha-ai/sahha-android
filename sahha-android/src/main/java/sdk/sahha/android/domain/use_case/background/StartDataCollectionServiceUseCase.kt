package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.domain.repository.BackgroundRepo

class StartDataCollectionServiceUseCase (
    private val repository: BackgroundRepo
) {
    operator fun invoke(icon: Int?, title: String?, shortDescription: String?, callback: ((error: String?, success: Boolean) -> Unit)?) {
        repository.startDataCollectionService(icon, title, shortDescription, callback)
    }
}