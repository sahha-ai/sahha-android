package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.AuthRepo
import javax.inject.Inject

class SaveTokensUseCase @Inject constructor(
    private val repository: AuthRepo
) {
    suspend operator fun invoke(token: String, refreshToken: String) {
        repository.saveTokens(token, refreshToken)
    }
}