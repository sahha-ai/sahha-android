package features.auth.infra

import features.auth.model.AuthResponse
import features.server.domain.JsonHttpPort
import kotlinx.serialization.Serializable

internal interface AuthApi {
    suspend fun register(appId: String, appSecret: String, body: RegisterBody): AuthResponse
}


internal class AuthApiImpl(
    private val httpProvider: () -> JsonHttpPort
) : AuthApi {
    override suspend fun register(
        appId: String,
        appSecret: String,
        body: RegisterBody
    ): AuthResponse {
        val http = httpProvider()
        return http.post<RegisterBody, AuthResponse>(
            path = "/api/v1/oauth/profile/register/appId",
            body = body,
            headers = mapOf("AppId" to appId, "AppSecret" to appSecret),
            requiresAuth = false // registering with app creds
        )
    }
}


@Serializable
internal data class RegisterBody(
    val externalId: String? = null
)