package features.server.infra.http

class ConnectivityException(cause: Throwable): RuntimeException(cause)
class RequestValidationException(msg: String): IllegalArgumentException(msg)
class HttpException(val code: Int, val responseBody: String, val responseHeaders: Map<String, List<String>>): RuntimeException("HTTP $code")
