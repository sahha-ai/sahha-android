package sdk.sahha.android.domain.model.categories

import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.source.SahhaSensorStatus

open class PermissionHandler {
    val activityCallback = ActivityCallback()
    var sensorStatus: Enum<SahhaSensorStatus> = SahhaSensorStatus.pending
}