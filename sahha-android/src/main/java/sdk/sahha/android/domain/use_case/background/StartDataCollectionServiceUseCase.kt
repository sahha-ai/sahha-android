package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.domain.manager.SahhaNotificationManager

class StartDataCollectionServiceUseCase(
    private val manager: SahhaNotificationManager
) {
    operator fun invoke(
        icon: Int?,
        title: String?,
        shortDescription: String?,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        manager.startDataCollectionService(icon, title, shortDescription, callback)
    }
}