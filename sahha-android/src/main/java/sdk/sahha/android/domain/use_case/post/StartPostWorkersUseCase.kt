package sdk.sahha.android.domain.use_case.post

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.Constants
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

internal class StartPostWorkersUseCase @Inject constructor(
    @IoScope private val ioScope: CoroutineScope,
    private val sensorRepo: SensorRepo,
    private val configRepo: SahhaConfigRepo,
    private val permissionManager: PermissionManager,
) {
    operator fun invoke(context: Context) {
        ioScope.launch {
            val config = configRepo.getConfig()

            permissionManager.getNativeSensorStatus(context) { status ->
                sensorRepo.checkAndStartWorker(config, SahhaSensor.device_lock.ordinal) {
                    sensorRepo.startDevicePostWorker(
                        Constants.FIFTEEN_MINUTES,
                        Constants.DEVICE_POST_WORKER_TAG
                    )
                }

                if (status == SahhaSensorStatus.enabled) {
                    sensorRepo.checkAndStartWorker(config, SahhaSensor.sleep.ordinal) {
                        sensorRepo.startSleepPostWorker(
                            Constants.FIFTEEN_MINUTES,
                            Constants.SLEEP_POST_WORKER_TAG
                        )
                    }
                    sensorRepo.checkAndStartWorker(config, SahhaSensor.step_count.ordinal) {
                        sensorRepo.startStepPostWorker(
                            Constants.FIFTEEN_MINUTES,
                            Constants.STEP_POST_WORKER_TAG
                        )
                    }
                }
            }
        }
    }
}