package features.server.domain

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


// Convenience overloads using your Json config
internal class JsonHttpPort(private val http: HttpPort, private val json: Json) {
    suspend inline fun <reified Res : Any> get(path: String, headers: Map<String,String> = emptyMap(), requiresAuth: Boolean = false): Res =
        http.get(path, headers, requiresAuth) { body -> json.decodeFromString(body) }

    suspend inline fun <reified Req: Any, reified Res : Any> post(path: String, body: Req, headers: Map<String,String> = emptyMap(), requiresAuth: Boolean = false): Res =
        http.post(path, body, headers, requiresAuth, { req -> json.encodeToString(req) }) { body -> json.decodeFromString(body) }

    suspend inline fun <reified Req: Any, reified Res : Any> put(path: String, body: Req, headers: Map<String,String> = emptyMap(), requiresAuth: Boolean = false): Res =
        http.put(path, body, headers, requiresAuth, { req -> json.encodeToString(req) }) { body -> json.decodeFromString(body) }

    suspend inline fun <reified Req: Any, reified Res : Any> patch(path: String, body: Req, headers: Map<String,String> = emptyMap(), requiresAuth: Boolean = false): Res =
        http.patch(path, body, headers, requiresAuth, { req -> json.encodeToString(req) }) { body -> json.decodeFromString(body) }

    suspend inline fun <reified Res : Any> delete(path: String, headers: Map<String,String> = emptyMap(), requiresAuth: Boolean = false): Res =
        http.delete(path, headers, requiresAuth) { body -> json.decodeFromString(body) }
}
