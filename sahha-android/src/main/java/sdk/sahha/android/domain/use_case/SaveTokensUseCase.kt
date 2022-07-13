package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.AuthRepo

class SaveTokensUseCase (
    private val repository: AuthRepo
) {
    suspend operator fun invoke(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        repository.saveTokens(profileToken, refreshToken, callback)
    }
}