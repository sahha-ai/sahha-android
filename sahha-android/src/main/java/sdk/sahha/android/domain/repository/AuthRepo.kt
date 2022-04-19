package sdk.sahha.android.domain.repository

interface AuthRepo {
    suspend fun saveTokens(
        token: String,
        refreshToken: String,
        callback: ((error: String?, success: String?) -> Unit)?
    )
}