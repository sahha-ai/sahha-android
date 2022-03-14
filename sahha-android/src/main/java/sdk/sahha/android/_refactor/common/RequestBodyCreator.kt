package sdk.sahha.android._refactor.common

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

internal object RequestBodyCreator {
    fun create(rbContent: HashMap<String, Any>): RequestBody {
        val jsonObject = JSONObject()

        for (content in rbContent) {
            jsonObject.put(content.key, content.value)
        }

        val jsonString = jsonObject.toString()
        return jsonString.toRequestBody("application/json".toMediaTypeOrNull())
    }
}