package sdk.sahha.android.domain.use_case.post

import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.interaction.AuthInteractionManager
import javax.inject.Inject

class PostAllSensorDataUseCase @Inject constructor(
    private val repository: SensorRepo,
    private val authRepo: AuthRepo,
    private val authManager: AuthInteractionManager
) {
    suspend operator fun invoke(
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        if (authManager.authIsInvalid(authRepo.getToken(), authRepo.getRefreshToken())) {
            callback(SahhaErrors.noToken, false)
            return
        }

        repository.postAllSensorData(callback)
    }
}