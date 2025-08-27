package features.server.infra.http

import features.server.domain.HttpPort
import features.server.infra.retry.RetryPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class NetworkConfig(
    val baseUrl: String,
    val userAgent: String = "SahhaSDK",
    val connectTimeoutMillis: Int = 10_000,
    val readTimeoutMillis: Int = 15_000
)

class HttpJsonClientAdapter(
    private val cfg: NetworkConfig,
    private val policy: RetryPolicy,
    private val interceptors: List<RequestInterceptor> = emptyList()
) : HttpPort {

    // ---------- HttpPort (JSON helpers provided by JsonHttpPort wrapper) ----------
    override suspend fun <Res : Any> get(
        pathOrUrl: String,
        headers: Map<String, String>,
        requiresAuth: Boolean,
        deserializer: (String) -> Res
    ): Res {
        val req = HttpRequest(HttpMethod.GET, pathOrUrl, mergeDefaultHeaders(false, headers), null, requiresAuth)
        val resp = executeWithRetry(req)
        if (!resp.isSuccessful) throw HttpException(resp.statusCode, resp.body, resp.headers)
        return deserializer(resp.body)
    }

    override suspend fun <Req : Any, Res : Any> post(
        pathOrUrl: String,
        body: Req,
        headers: Map<String, String>,
        requiresAuth: Boolean,
        serializer: (Req) -> String,
        deserializer: (String) -> Res
    ): Res {
        return sendWithBody(HttpMethod.POST, pathOrUrl, body, headers, requiresAuth, serializer, deserializer)
    }

    override suspend fun <Req : Any, Res : Any> put(
        pathOrUrl: String,
        body: Req,
        headers: Map<String, String>,
        requiresAuth: Boolean,
        serializer: (Req) -> String,
        deserializer: (String) -> Res
    ): Res {
        return sendWithBody(HttpMethod.PUT, pathOrUrl, body, headers, requiresAuth, serializer, deserializer)
    }

    override suspend fun <Req : Any, Res : Any> patch(
        pathOrUrl: String,
        body: Req,
        headers: Map<String, String>,
        requiresAuth: Boolean,
        serializer: (Req) -> String,
        deserializer: (String) -> Res
    ): Res {
        return sendWithBody(HttpMethod.PATCH, pathOrUrl, body, headers, requiresAuth, serializer, deserializer)
    }

    override suspend fun <Res : Any> delete(
        pathOrUrl: String,
        headers: Map<String, String>,
        requiresAuth: Boolean,
        deserializer: (String) -> Res
    ): Res {
        val req = HttpRequest(HttpMethod.DELETE, pathOrUrl, mergeDefaultHeaders(false, headers), null, requiresAuth)
        val resp = executeWithRetry(req)
        if (!resp.isSuccessful) throw HttpException(resp.statusCode, resp.body, resp.headers)
        return deserializer(resp.body)
    }

    private suspend fun <Req : Any, Res : Any> sendWithBody(
        method: HttpMethod,
        pathOrUrl: String,
        body: Req,
        headers: Map<String, String>,
        requiresAuth: Boolean,
        serializer: (Req) -> String,
        deserializer: (String) -> Res
    ): Res {
        require(method in setOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)) {
            "HTTP $method must not include a request body"
        }
        val payload = serializer(body)
        val req = HttpRequest(method, pathOrUrl, mergeDefaultHeaders(true, headers), payload, requiresAuth)
        val resp = executeWithRetry(req)
        if (!resp.isSuccessful) throw HttpException(resp.statusCode, resp.body, resp.headers)
        return deserializer(resp.body)
    }

    // ---------- Transport + Retry ----------
    private suspend fun executeWithRetry(original: HttpRequest): HttpResponse {
        var attempt = 0
        var lastException: Throwable? = null

        while (true) {
            try {
                val intercepted = applyInterceptors(original)
                return executeOnce(intercepted)
            } catch (ex: Throwable) {
                val (retryAfterMs, retryable) = classifyForRetry(ex)
                if (!retryable) throw ex
                val delayMs = policy.nextDelayMs(attempt, retryAfterMs)
                if (delayMs == null) throw (lastException ?: ex)
                lastException = ex
                attempt++
                delay(delayMs)
            }
        }
    }

    private fun classifyForRetry(ex: Throwable): Pair<Long?, Boolean> {
        // Transport failures: retry
        if (ex is ConnectivityException) return null to true
        // HTTP failures are thrown as HttpException. Parse retryable codes from message or rethrow fallback.
        if (ex is HttpException) {
            val code = ex.code
            val retryable = code == 429 || code in 500..599
            val retryAfterMs = parseRetryAfterMillis(ex.responseHeaders)
            return (retryAfterMs) to retryable
        }
        return null to false
    }

    private fun parseRetryAfterMillis(headers: Map<String, List<String>>): Long? {
        val values = headers.entries.firstOrNull { it.key.equals("Retry-After", ignoreCase = true) }?.value ?: return null
        val raw = values.firstOrNull() ?: return null
        // either seconds or HTTP-date; keep it simple for now (seconds)
        return raw.toLongOrNull()?.let { it * 1000 }
    }

    private suspend fun executeOnce(request: HttpRequest): HttpResponse = withContext(Dispatchers.IO) {
        val fullUrl = resolveUrl(request.urlOrPath, cfg.baseUrl)
        val conn = (URL(fullUrl).openConnection() as HttpURLConnection)
        try {
            configure(conn)
            conn.requestMethod = request.method.name
            conn.setRequestProperty("User-Agent", cfg.userAgent)
            request.headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }

            val hasBody = request.body != null
            if (hasBody) {
                require(request.method in setOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)) {
                    "HTTP ${request.method} must not include a request body"
                }
                val bytes = request.body!!.toByteArray(Charsets.UTF_8)
                conn.doOutput = true
                conn.setFixedLengthStreamingMode(bytes.size)
                conn.outputStream.use { it.write(bytes) }
            }

            val code = conn.responseCode
            val headers = conn.headerFields.filterKeys { it != null }.mapKeys { it.key!! }
            val body = readBody(conn)
            HttpResponse(code, body, headers)
        } catch (ioe: IOException) {
            throw ConnectivityException(ioe)
        } finally {
            conn.disconnect()
        }
    }

    private fun configure(c: HttpURLConnection) {
        c.connectTimeout = cfg.connectTimeoutMillis
        c.readTimeout = cfg.readTimeoutMillis
        c.useCaches = false
        c.instanceFollowRedirects = false
    }

    private fun readBody(c: HttpURLConnection): String {
        val stream = if (c.responseCode in 200..299) c.inputStream else (c.errorStream ?: c.inputStream)
        return stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText) ?: ""
    }

    private suspend fun applyInterceptors(original: HttpRequest): HttpRequest {
        var current = original
        for (i in interceptors) {
            current = i.intercept(current)
        }
        return current
    }

    private fun resolveUrl(urlOrPath: String, baseUrl: String): String {
        val trimmedBaseUrl = baseUrl.trimEnd('/')
        return if (urlOrPath.lowercase(Locale.ROOT).startsWith("http://") ||
            urlOrPath.lowercase(Locale.ROOT).startsWith("https://")) {
            urlOrPath
        } else {
            val trimmedPath = urlOrPath.trimStart('/')
            "$trimmedBaseUrl/$trimmedPath"
        }
    }

    private fun mergeDefaultHeaders(hasBody: Boolean, headers: Map<String, String>): Map<String, String> {
        val merged = LinkedHashMap<String, String>()
        merged["Accept"] = "application/json"
        if (hasBody && headers.keys.none { it.equals("Content-Type", ignoreCase = true) }) {
            merged["Content-Type"] = "application/json; charset=utf-8"
        }
        merged.putAll(headers)
        return merged
    }
}
