package features.server.infra.http

fun interface RequestInterceptor {
    suspend fun intercept(request: HttpRequest): HttpRequest
}