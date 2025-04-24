package sdk.sahha.android.framework.processor

import android.content.Context
import androidx.health.connect.client.records.metadata.Device
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.AppEventEnum
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.manager.IdManager
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import sdk.sahha.android.domain.model.app_event.AppEvent
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.processor.AppEventProcessor
import sdk.sahha.android.source.Sahha
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

private const val TAG = "AppEventProcessorImpl"

internal class AppEventProcessorImpl @Inject constructor(
    private val context: Context,
    private val mapper: HealthConnectConstantsMapper,
    private val manager: IdManager
) : AppEventProcessor {
    private val appEventCache = mutableListOf<AppEvent>()

    override suspend fun process(event: AppEvent): SahhaDataLog? {
        return when (event.event) {
            AppEventEnum.APP_RESUME -> {
                appEventCache.add(event)
                null
            }

            AppEventEnum.APP_PAUSE -> {
                linkStartEvent(event, AppEventEnum.APP_RESUME)
            }

            else -> null
        }
    }

    private fun linkStartEvent(
        event: AppEvent,
        matchingEvent: AppEventEnum
    ): SahhaDataLog? {
        appEventCache.forEach { cachedEvent ->
            val containsMatchingEvent = cachedEvent.event == matchingEvent

            if (containsMatchingEvent) {
                val startDateTime = cachedEvent.dateTime
                val endDateTime = event.dateTime

                removeAllCachedMatchingEvent(matchingEvent)

                return eventToSahhaDataLogStartEndTimes(
                    start = startDateTime,
                    end = endDateTime,
                )
            }
        }

        // Found no matched event
        return null
    }

    private fun removeAllCachedMatchingEvent(matchingEvent: AppEventEnum) {
        appEventCache.removeAll { event -> event.event == matchingEvent }
    }

    private fun eventToSahhaDataLogStartEndTimes(
        start: ZonedDateTime,
        end: ZonedDateTime,
    ): SahhaDataLog {
        val startIso = Sahha.di.timeManager.instantToIsoTime(start.toInstant())
        val endIso = Sahha.di.timeManager.instantToIsoTime(end.toInstant())

        val startEpochMilliDouble = start.toInstant().toEpochMilli().toDouble()
        val endEpochMilliDouble = end.toInstant().toEpochMilli().toDouble()
        val durationMillis = endEpochMilliDouble - startEpochMilliDouble
        val durationSeconds = durationMillis / 1000

        return SahhaDataLog(
            id = UUID.nameUUIDFromBytes(
                (Constants.APP_SESSION + start + end).toByteArray()
            ).toString(),
            logType = Constants.DataLogs.DEVICE,
            dataType = Constants.APP_SESSION,
            source = context.packageName,
            value = durationSeconds,
            unit = Constants.DataUnits.SECOND,
            startDateTime = startIso,
            endDateTime = endIso,
            recordingMethod = RecordingMethods.automatically_recorded.name,
            deviceId = manager.getDeviceId(),
            deviceType = mapper.devices(Device.TYPE_PHONE),
        )
    }
}