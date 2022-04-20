package sdk.sahha.android.domain.repository

interface AuthRepo {
    suspend fun saveTokens(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)?
    )
}