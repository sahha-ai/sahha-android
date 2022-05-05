package sdk.sahha.android.domain.model.categories

import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

open class RequiresPermission @Inject constructor(
    private val setPermissionLogicUseCase: SetPermissionLogicUseCase,
) {
    val activityCallback = ActivityCallback()
    var sensorStatus: Enum<SahhaSensorStatus> = SahhaSensorStatus.pending
}