package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.repository.BackgroundRepo
import javax.inject.Inject

class StartCollectingStepDetectorData @Inject constructor(
    val repository: BackgroundRepo
) {
    suspend operator fun invoke(
        movementDao: MovementDao,
        stepDetectorRegistered: Boolean
    ): Boolean {
        return repository.startStepDetectorAsync(
            movementDao, stepDetectorRegistered
        )
    }
}