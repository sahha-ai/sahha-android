package sdk.sahha.android.domain.model.categories

import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

internal data class PermissionHandler @Inject constructor(
    val activityCallback: ActivityCallback,
    var sensorStatus: Enum<SahhaSensorStatus> = SahhaSensorStatus.pending
)