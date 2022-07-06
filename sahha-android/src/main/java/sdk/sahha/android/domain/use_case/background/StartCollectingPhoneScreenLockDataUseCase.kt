package sdk.sahha.android.domain.use_case.background

import android.content.Context
import sdk.sahha.android.domain.repository.BackgroundRepo
import javax.inject.Inject

class StartCollectingPhoneScreenLockDataUseCase @Inject constructor(
    private val repository: BackgroundRepo
) {
    operator fun invoke(
        serviceContext: Context,
    ) {
        repository.startPhoneScreenReceivers(serviceContext)
    }
}