package sdk.sahha.android.domain.model.processor

import sdk.sahha.android.domain.model.app_event.AppEvent
import sdk.sahha.android.domain.model.data_log.SahhaDataLog

internal interface AppEventProcessor {
    suspend fun process(event: AppEvent): SahhaDataLog?
}