package sdk.sahha.android._refactor.domain.repository

interface AuthRepo {
    suspend fun authenticate(customerId: String, profileId: String)
}