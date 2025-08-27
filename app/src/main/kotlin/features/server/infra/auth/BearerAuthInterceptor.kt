package features.server.infra.auth

import features.auth.domain.AuthPort
import features.server.infra.http.HttpRequest
import features.server.infra.http.RequestInterceptor

class BearerAuthInterceptor(private val auth: AuthPort) : RequestInterceptor {
    override suspend fun intercept(request: HttpRequest): HttpRequest {
        if (!request.requiresAuth) return request
        val token = runCatching { auth.getValidToken() }.getOrNull() ?: return request
        val headers = LinkedHashMap(request.headers)
        headers["Authorization"] = "profile $token"
        return request.copy(headers = headers)
    }
}