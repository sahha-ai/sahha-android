package sdk.sahha.android._refactor.presentation

import dagger.hilt.android.AndroidEntryPoint
import sdk.sahha.android._refactor.domain.repository.AuthRepo
import sdk.sahha.android._refactor.domain.use_case.AuthenticateUseCase
import javax.inject.Inject

@AndroidEntryPoint
object Sahha {
    @Inject
    lateinit var authRepo: AuthRepo

    suspend fun authenticate(customerId: String, profileId: String) {
        AuthenticateUseCase(authRepo).invoke(customerId, profileId)
    }
}