package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.domain.manager.SahhaNotificationManager
import javax.inject.Inject

class StartDataCollectionServiceUseCase @Inject constructor (
    private val manager: SahhaNotificationManager
) {
    operator fun invoke(
        icon: Int? = null,
        title: String? = null,
        shortDescription: String? = null,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        manager.startDataCollectionService(icon, title, shortDescription, callback)
    }
}