package sdk.sahha.android.domain.repository

interface AuthRepo {
    suspend fun authenticate(profileId: String, callback: ((error: String?, success: String?) -> Unit))
}