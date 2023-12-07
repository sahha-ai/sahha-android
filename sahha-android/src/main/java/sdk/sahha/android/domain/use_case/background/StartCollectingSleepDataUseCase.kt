package sdk.sahha.android.domain.use_case.background

import android.content.Context
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

class StartCollectingSleepDataUseCase @Inject constructor(
    private val repository: SensorRepo,
    private val permissionManager: PermissionManager
) {
    operator fun invoke(context: Context) {
        permissionManager.getNativeSensorStatus(context) { status ->
            if (status == SahhaSensorStatus.enabled) {
                repository.startSleepWorker(
                    360,
                    Constants.SLEEP_WORKER_TAG
                )
            }
        }
    }
}