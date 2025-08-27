package features.server.infra.http

data class HttpResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, List<String>> = emptyMap()
) {
    val isSuccessful: Boolean get() = statusCode in 200..299
}

enum class HttpMethod { GET, POST, PUT, PATCH, DELETE }

data class HttpRequest(
    val method: HttpMethod,
    val urlOrPath: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val requiresAuth: Boolean = false
)
