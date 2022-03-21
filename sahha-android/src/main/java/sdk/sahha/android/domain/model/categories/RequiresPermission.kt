package sdk.sahha.android.domain.model.categories

import sdk.sahha.android.domain.model.ActivityCallback
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import javax.inject.Inject

open class RequiresPermission @Inject constructor(
    private val setPermissionLogicUseCase: SetPermissionLogicUseCase
) {
    val activityCallback = ActivityCallback()

    fun setPermissionLogic() {
        setPermissionLogicUseCase()
    }
}