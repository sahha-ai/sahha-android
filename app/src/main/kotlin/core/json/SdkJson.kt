package core.json

import kotlinx.serialization.json.Json

internal object SdkJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        isLenient = true
    }
}