package features.server.domain

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


internal interface HttpPort {
    suspend fun <Res : Any> get(
        pathOrUrl: String,
        headers: Map<String, String> = emptyMap(),
        requiresAuth: Boolean = false,
        deserializer: (String) -> Res
    ): Res

    suspend fun <Req : Any, Res : Any> post(
        pathOrUrl: String,
        body: Req,
        headers: Map<String, String> = emptyMap(),
        requiresAuth: Boolean = false,
        serializer: (Req) -> String,
        deserializer: (String) -> Res
    ): Res

    suspend fun <Req : Any, Res : Any> put(
        pathOrUrl: String,
        body: Req,
        headers: Map<String, String> = emptyMap(),
        requiresAuth: Boolean = false,
        serializer: (Req) -> String,
        deserializer: (String) -> Res
    ): Res

    suspend fun <Req : Any, Res : Any> patch(
        pathOrUrl: String,
        body: Req,
        headers: Map<String, String> = emptyMap(),
        requiresAuth: Boolean = false,
        serializer: (Req) -> String,
        deserializer: (String) -> Res
    ): Res

    suspend fun <Res : Any> delete(
        pathOrUrl: String,
        headers: Map<String, String> = emptyMap(),
        requiresAuth: Boolean = false,
        deserializer: (String) -> Res
    ): Res
}
