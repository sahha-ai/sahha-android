package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.repository.SensorRepo

class StartCollectingSleepDataUseCase (
    private val repository: SensorRepo
) {
    operator fun invoke() {
        repository.startSleepWorker(
            360,
            Constants.SLEEP_WORKER_TAG
        )
    }
}