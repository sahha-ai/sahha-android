package sdk.sahha.android.domain.repository

interface AuthRepo {
    fun authenticate(customerId: String, profileId: String)
}