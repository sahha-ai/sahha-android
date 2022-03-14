package sdk.sahha.android._refactor.domain.repository

internal interface AuthRepo {
    suspend fun authenticate(customerId: String, profileId: String)
}