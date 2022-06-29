package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.repository.BackgroundRepo
import javax.inject.Inject

class StartCollectingStepCounterData @Inject constructor(
    private val repository: BackgroundRepo
) {
    suspend operator fun invoke(
        movementDao: MovementDao,
        stepCounterRegistered: Boolean
    ): Boolean {
        return repository.startStepCounterAsync(
            movementDao, stepCounterRegistered
        )
    }
}