package sdk.sahha.android.domain.model.categories

import android.content.Context
import androidx.activity.ComponentActivity
import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.use_case.permissions.OpenAppSettingsUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import sdk.sahha.android.source.SahhaActivityStatus
import javax.inject.Inject

open class RequiresPermission @Inject constructor(
    private val setPermissionLogicUseCase: SetPermissionLogicUseCase, ) {
    val activityCallback = ActivityCallback()
    var activityStatus: Enum<SahhaActivityStatus> = SahhaActivityStatus.pending

    fun prepareActivity(activity: ComponentActivity) {
        setPermissionLogicUseCase(activity)
    }
}