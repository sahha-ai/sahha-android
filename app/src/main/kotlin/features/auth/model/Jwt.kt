package features.auth.model

import android.util.Base64
import org.json.JSONObject

internal object Jwt {
    fun decodeExpSecondsOrNull(jwt: String): Long? = runCatching {
        val parts = jwt.split('.')
        if (parts.size < 2) return null
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
        val json = JSONObject(payload)
        if (!json.has("exp")) return null
        json.getLong("exp")
    }.getOrNull()
}