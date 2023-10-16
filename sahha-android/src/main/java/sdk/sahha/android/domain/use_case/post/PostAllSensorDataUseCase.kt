package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.interaction.AuthInteractionManager
import javax.inject.Inject

private const val tag = "PostAllSensorDataUseCase"
class PostAllSensorDataUseCase @Inject constructor(
    private val repository: SensorRepo,
    private val authManager: AuthInteractionManager,
    private val sahhaErrorLogger: SahhaErrorLogger
) {
    suspend operator fun invoke(
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        if (!authManager.checkIsAuthenticated()) {
            callback(SahhaErrors.noToken, false)
            sahhaErrorLogger.application(
                SahhaErrors.noToken,
                tag,
                "PostAllSensorDataUseCase",
            )
            return
        }

        repository.postAllSensorData(callback)
    }
}