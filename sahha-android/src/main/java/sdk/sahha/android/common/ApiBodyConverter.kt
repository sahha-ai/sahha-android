package sdk.sahha.android.common

import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import org.json.JSONObject
import sdk.sahha.android.source.Sahha

object ApiBodyConverter {
    fun responseBodyToJson(response: ResponseBody?): JSONObject? {
        response?.also {
            val jsonString = it.string()
            return JSONObject(jsonString)
        }
        return null
    }

    fun requestBodyToJson(request: RequestBody?): JSONObject? {
        request?.also {
            val buffer = Buffer()
            it.writeTo(buffer)
            val rbString = buffer.readUtf8()
            return JSONObject(rbString)
        }
        return null
    }

    fun requestBodyToString(request: RequestBody?): String? {
        try {
            request?.also {
                val json = requestBodyToJson(it)
                val filteredJson = hideSensitiveData(json, arrayOf("profileToken", "refreshToken"))
                return filteredJson.toString()
            }
        } catch (e: Exception) {
            e.message?.also {
                Sahha.di.sahhaErrorLogger.application(it)
            }
        }
        return null
    }

    // This is case sensitive
    private fun hideSensitiveData(json: JSONObject?, sensitiveData: Array<String>): JSONObject? {
        json?.also { _json ->
            _json.keys().forEach { keyName ->
                if (sensitiveData.contains(keyName)) {
                    _json.put(keyName, "********")
                }
            }
            return _json
        }
        return null
    }
}