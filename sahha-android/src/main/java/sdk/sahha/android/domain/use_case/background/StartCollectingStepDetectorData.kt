package sdk.sahha.android.domain.use_case.background

import android.content.Context
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject

internal class StartCollectingStepDetectorData @Inject constructor (
    val repository: SensorRepo
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