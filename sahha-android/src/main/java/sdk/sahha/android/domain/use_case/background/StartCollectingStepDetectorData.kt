package sdk.sahha.android.domain.use_case.background

import android.content.Context
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.repository.BackgroundRepo

class StartCollectingStepDetectorData (
    val repository: BackgroundRepo
) {
    suspend operator fun invoke(
        context: Context,
        movementDao: MovementDao,
    ) {
        repository.startStepDetectorAsync(
            context, movementDao
        )
    }
}