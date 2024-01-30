package sdk.sahha.android.domain.use_case.background

import android.content.Context
import sdk.sahha.android.domain.manager.ReceiverManager
import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject

internal class StartCollectingPhoneScreenLockDataUseCase @Inject constructor (
    private val manager: ReceiverManager
) {
    operator fun invoke(
        serviceContext: Context,
    ) {
        manager.startPhoneScreenReceivers(serviceContext)
    }
}