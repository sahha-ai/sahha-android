package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.AuthRepo
import javax.inject.Inject

class AuthenticateUseCase @Inject constructor(
    private val repository: AuthRepo
) {
    suspend operator fun invoke(profileId: String, callback: ((error: String?, success: String?) -> Unit)) {
        repository.authenticate(profileId, callback)
    }
}