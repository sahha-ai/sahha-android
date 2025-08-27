package features.server.infra.interceptors

import features.auth.domain.AuthPort
import features.server.infra.http.HttpRequest
import features.server.infra.http.RequestInterceptor

class BearerAuthInterceptor(
    private val authProvider: () -> AuthPort
) : RequestInterceptor {
    override suspend fun intercept(request: HttpRequest): HttpRequest {
        if (!request.requiresAuth) return request
        val auth = authProvider()
        val token = runCatching { auth.getValidToken() }.getOrNull() ?: return request
        val headers = LinkedHashMap(request.headers)
        headers["Authorization"] = "profile $token"
        return request.copy(headers = headers)
    }
}