package sdk.sahha.android.domain.model.app_event

import sdk.sahha.android.domain.internal_enum.AppEventEnum
import java.time.ZonedDateTime

internal data class AppEvent(
    val event: AppEventEnum,
    val dateTime: ZonedDateTime,
)