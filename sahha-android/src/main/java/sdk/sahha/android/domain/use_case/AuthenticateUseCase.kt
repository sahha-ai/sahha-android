package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.AuthRepo
import javax.inject.Inject

class AuthenticateUseCase @Inject constructor(
    private val repository: AuthRepo
) {
    operator fun invoke(customerId: String, profileId: String) {
        repository.authenticate(customerId, profileId)
    }
}