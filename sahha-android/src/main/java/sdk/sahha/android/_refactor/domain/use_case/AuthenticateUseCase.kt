package sdk.sahha.android._refactor.domain.use_case

import sdk.sahha.android._refactor.domain.repository.AuthRepo
import javax.inject.Inject

internal class AuthenticateUseCase @Inject constructor(
    private val repository: AuthRepo
) {
    suspend operator fun invoke(customerId: String, profileId: String) {
        repository.authenticate(customerId, profileId)
    }
}