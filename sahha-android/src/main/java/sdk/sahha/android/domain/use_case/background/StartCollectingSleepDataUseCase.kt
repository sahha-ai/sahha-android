package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject

class StartCollectingSleepDataUseCase @Inject constructor (
    private val repository: SensorRepo
) {
    operator fun invoke() {
        repository.startSleepWorker(
            360,
            Constants.SLEEP_WORKER_TAG
        )
    }
}