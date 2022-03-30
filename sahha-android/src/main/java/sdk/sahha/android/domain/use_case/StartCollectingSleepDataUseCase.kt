package sdk.sahha.android.domain.use_case

import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.repository.BackgroundRepo
import javax.inject.Inject

class StartCollectingSleepDataUseCase @Inject constructor(
    private val repository: BackgroundRepo
) {
    operator fun invoke() {
        repository.startSleepWorker(
            360,
            Constants.SLEEP_WORKER_TAG
        )
    }
}