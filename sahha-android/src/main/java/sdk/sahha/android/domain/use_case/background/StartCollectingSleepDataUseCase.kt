package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.repository.BackgroundRepo

class StartCollectingSleepDataUseCase (
    private val repository: BackgroundRepo
) {
    operator fun invoke() {
        repository.startSleepWorker(
            360,
            Constants.SLEEP_WORKER_TAG
        )
    }
}