package sdk.sahha.android.source

import android.content.Context
import androidx.annotation.Keep
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import sdk.sahha.android.data.remote.dto.SleepDto
import sdk.sahha.android.data.remote.dto.StepDto
import sdk.sahha.android.data.remote.dto.send.PhoneUsageSendDto
import sdk.sahha.android.data.remote.dto.send.SleepSendDto
import sdk.sahha.android.data.remote.dto.toSleepSendDto
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.device.toPhoneUsageSendDto
import sdk.sahha.android.domain.model.error_log.SahhaResponseError
import sdk.sahha.android.domain.model.error_log.SahhaResponseErrorItem
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.toStepDto

@Keep
object SahhaConverterUtility {
    fun hashMapToRequestBody(rbContent: HashMap<String, String>): RequestBody {
        val jsonObject = JSONObject()

        for (content in rbContent) {
            jsonObject.put(content.key, content.value)
        }

        val jsonString = jsonObject.toString()

        return jsonString.toRequestBody("application/json".toMediaTypeOrNull())
    }

    fun responseBodyToJson(response: ResponseBody?): JSONObject? {
        response?.also {
            val jsonString = it.string()
            return JSONObject(jsonString)
        }
        return null
    }

    private fun convertErrorItemErrorsToList(errorsJsonArray: JSONArray): List<String> {
        val errors = mutableListOf<String>()

        for (j in 0 until errorsJsonArray.length()) {
            errors.add(errorsJsonArray[j].toString())
        }

        return errors
    }

    private fun convertErrorItemsToList(errorItemsJsonArray: JSONArray): List<SahhaResponseErrorItem> {
        val items = mutableListOf<SahhaResponseErrorItem>()

        for (i in 0 until errorItemsJsonArray.length()) {
            val jsonObject = errorItemsJsonArray.getJSONObject(i)
            val errorsJsonArray = jsonObject.getJSONArray("errors")
            val errors = convertErrorItemErrorsToList(errorsJsonArray)

            items.add(
                SahhaResponseErrorItem(
                    jsonObject.get("origin") as String,
                    errors
                )
            )
        }

        return items
    }

    internal fun responseBodyToSahhaResponseError(response: ResponseBody?): SahhaResponseError? {
        response?.also { rb ->
            return try {
                val errorBodyJson = responseBodyToJson(rb)
                val errorItemsJsonArray = errorBodyJson?.get("errors") as JSONArray
                val errorItems = convertErrorItemsToList(errorItemsJsonArray)

                SahhaResponseError(
                    errorBodyJson.get("title") as String,
                    errorBodyJson.get("statusCode") as Int,
                    errorBodyJson.get("location") as String,
                    errorItems,
                )
            } catch (e: Exception) {
                null
            }
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
                Sahha.di.sahhaErrorLogger.application(
                    it,
                    "requestBodyToString",
                    request?.toString()
                )
            }
        }
        return null
    }

    internal fun stepDataToStepDto(stepData: List<StepData>): List<StepDto> {
        val createdAt = Sahha.di.timeManager.nowInISO()
        return stepData.map { it.toStepDto(createdAt) }
    }

    internal fun sleepDtoToSleepSendDto(sleepData: List<SleepDto>): List<SleepSendDto> {
        return sleepData.map {
            it.toSleepSendDto()
        }
    }

    internal fun phoneUsageToPhoneUsageSendDto(usageData: List<PhoneUsage>): List<PhoneUsageSendDto> {
        return usageData.map {
            it.toPhoneUsageSendDto()
        }
    }

    fun stringToDrawableResource(context: Context, iconString: String?): Int? {
        return try {
            context.resources.getIdentifier(iconString, "drawable", context.packageName)
        } catch (e: Exception) {
            null
        }
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