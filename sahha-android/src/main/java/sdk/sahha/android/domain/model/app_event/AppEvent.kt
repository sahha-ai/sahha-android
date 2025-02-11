package sdk.sahha.android.domain.model.app_event

import java.time.ZonedDateTime

internal data class AppEvent(
    val event: String,
    val dateTime: ZonedDateTime,
)