package sdk.sahha.android.source

import android.content.Context
import androidx.annotation.Keep
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.remote.dto.SleepDto
import sdk.sahha.android.data.remote.dto.send.*
import sdk.sahha.android.data.remote.dto.toSleepSendDto
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.device.toPhoneUsageSendDto
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.model.device_info.toDeviceInformationSendDto
import sdk.sahha.android.domain.model.error_log.SahhaResponseError
import sdk.sahha.android.domain.model.error_log.SahhaResponseErrorItem
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.toStepDto
import java.time.ZoneOffset

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

    internal fun stepDataToStepDto(stepData: List<StepData>): List<StepSendDto> {
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

    internal fun deviceInfoToDeviceInfoSendDto(deviceInfo: DeviceInformation): DeviceInformationSendDto {
        return deviceInfo.toDeviceInformationSendDto()
    }

    // Health Connect conversions
    internal fun sleepSessionToSleepDto(
        sleepSessionData: List<SleepSessionRecord>,
        createdAt: String
    ): List<SleepDto> {
        return sleepSessionData.map {
            SleepDto(
                id = -1,
                source = Constants.HEALTH_CONNECT_SLEEP_SESSION_DATA_SOURCE,
                sleepStage = "asleep",
                durationInMinutes = timeManager.calculateDurationFromInstant(it.startTime, it.endTime) ,
                startDateTime = timeManager.instantToIsoTime(it.startTime, it.startZoneOffset),
                endDateTime = timeManager.instantToIsoTime(it.endTime, it.endZoneOffset),
                createdAt = createdAt
            )
        }
    }

    internal fun sleepStageToSleepDto(
        sleepStageData: List<SleepStageRecord>,
        createdAt: String
    ): List<SleepDto> {
        return sleepStageData.map {
            SleepDto(
                id = -1,
                source = Constants.HEALTH_CONNECT_SLEEP_STAGE_DATA_SOURCE,
                sleepStage = it.stage,
                durationInMinutes = timeManager.calculateDurationFromInstant(it.startTime, it.endTime),
                startDateTime = timeManager.instantToIsoTime(it.startTime, it.startZoneOffset),
                endDateTime = timeManager.instantToIsoTime(it.endTime, it.endZoneOffset),
                createdAt = createdAt
            )
        }
    }

    internal fun healthConnectStepToStepDto(
        stepData: List<StepsRecord>,
        createdAt: String
    ): List<StepSendDto> {
        return stepData.map {
            StepSendDto(
                dataType = Constants.HEALTH_CONNECT_STEP_DATA_TYPE,
                count = it.count.toInt(),
                source = Constants.HEALTH_CONNECT_STEP_DATA_SOURCE,
                manuallyEntered = false,
                startDateTime = timeManager.instantToIsoTime(it.startTime, it.startZoneOffset),
                endDateTime = timeManager.instantToIsoTime(it.endTime, it.endZoneOffset),
                createdAt = createdAt
            )
        }
    }

    internal fun heartRateToHeartRateSendDto(
        heartRateData: List<HeartRateRecord>,
        createdAt: String
    ): List<HeartRateSendDto> {
        return heartRateData.map { record ->
            HeartRateSendDto(
                startDateTime = timeManager.instantToIsoTime(
                    record.startTime,
                    record.startZoneOffset
                ),
                endDateTime = timeManager.instantToIsoTime(record.endTime, record.endZoneOffset),
                samples = heartRateSampleToHeartRateSampleSendDto(
                    record.samples,
                    record.startZoneOffset,
                    createdAt
                )
            )
        }
    }

    private fun heartRateSampleToHeartRateSampleSendDto(
        heartRateSamples: List<HeartRateRecord.Sample>,
        timeOffset: ZoneOffset?,
        createdAt: String
    ): List<HeartRateSampleSendDto> {
        return heartRateSamples.map { sample ->
            HeartRateSampleSendDto(
                beatsPerMinute = sample.beatsPerMinute,
                timestamp = timeManager.instantToIsoTime(sample.time, timeOffset),
                createdAt = createdAt
            )
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