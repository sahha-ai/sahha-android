package sdk.sahha.android.source

import android.content.Context
import android.icu.text.DateFormat
import androidx.annotation.Keep
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.device.toPhoneUsageSendDto
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.model.device_info.toDeviceInformationSendDto
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.domain.model.dto.send.DeviceInformationDto
import sdk.sahha.android.domain.model.dto.send.PhoneUsageSendDto
import sdk.sahha.android.domain.model.dto.send.SleepSendDto
import sdk.sahha.android.domain.model.dto.toSleepSendDto
import sdk.sahha.android.domain.model.error_log.SahhaResponseError
import sdk.sahha.android.domain.model.error_log.SahhaResponseErrorItem
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.toStepDto
import java.time.Instant
import java.time.ZoneOffset

private const val tag = "SahhaConverterUtility"

@Keep
object SahhaConverterUtility {
    private val timeManager by lazy { Sahha.di.timeManager }

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

    fun responseBodyToJsonString(response: ResponseBody?): String? {
        return response?.string()
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

    fun requestBodyToJsonArray(request: RequestBody?): JSONArray? {
        request?.also {
            val buffer = Buffer()
            it.writeTo(buffer)
            val rbString = buffer.readUtf8()
            return JSONArray(rbString)
        }
        return null
    }

    fun requestBodyToString(request: RequestBody?): String? {
        return try {
            val json = requestBodyToJson(request)
            val filteredJson = hideSensitiveData(json, arrayOf("profileToken", "refreshToken"))
            filteredJson.toString()
        } catch (e: Exception) {
            null
        }
    }

    fun requestBodyArrayToString(request: RequestBody?): String? {
        return try {
            val array = requestBodyToJsonArray(request)
            array.toString()
        } catch (e: Exception) {
            null
        }
    }

    internal fun stepDataToStepDto(stepData: List<StepData>): List<StepDto> {
        return stepData.map { it.toStepDto() }
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

    internal fun deviceInfoToDeviceInfoSendDto(deviceInfo: DeviceInformation): DeviceInformationDto {
        return deviceInfo.toDeviceInformationSendDto()
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

    internal fun <T> convertToJsonString(
        records: List<T>?,
        usePrettyPrinting: Boolean = true
    ): String {
        return GsonBuilder().apply {
            if (usePrettyPrinting) setPrettyPrinting()
            registerTypeAdapter(
                Instant::class.java,
                JsonSerializer<Instant> { src, _, _ ->
                    JsonPrimitive(src.toString())
                }
            )
            registerTypeAdapter(
                ZoneOffset::class.java,
                JsonSerializer<ZoneOffset> { src, _, _ ->
                    JsonPrimitive(src.toString())
                }
            )
            setDateFormat(DateFormat.TIMEZONE_ISO_FIELD)
        }.create().toJson(records)
    }

    internal fun <T> convertToJsonString(anyObject: T?): String {
        return try {
            GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(
                    Instant::class.java,
                    JsonSerializer<Instant> { src, _, _ ->
                        JsonPrimitive(src.toString())
                    }
                )
                .registerTypeAdapter(
                    ZoneOffset::class.java,
                    JsonSerializer<ZoneOffset> { src, _, _ ->
                        JsonPrimitive(src.toString())
                    }
                )
                .setDateFormat(DateFormat.TIMEZONE_ISO_FIELD)
                .create()
                .toJson(anyObject)
        } catch (e: Exception) {
            println(e.stackTraceToString())
            e.message ?: "Something went wrong"
        }
    }
}