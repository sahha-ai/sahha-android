package sdk.sahha.android.domain.use_case.background

import android.content.Context
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.repository.BackgroundRepo
import javax.inject.Inject

class StartCollectingStepCounterData @Inject constructor(
    private val repository: BackgroundRepo
) {
    suspend operator fun invoke(
        context: Context,
        movementDao: MovementDao,
        stepCounterRegistered: Boolean
    ): Boolean {
        return repository.startStepCounterAsync(
            context, movementDao, stepCounterRegistered
        )
    }
}