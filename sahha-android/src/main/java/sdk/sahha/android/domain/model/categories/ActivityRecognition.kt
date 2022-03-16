package sdk.sahha.android.domain.model.categories

import sdk.sahha.android.domain.model.enums.PermissionStatus
import sdk.sahha.android.domain.use_case.permissions.GrantActivityRecognitionPermissionUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import javax.inject.Inject

class ActivityRecognition @Inject constructor(
    setPermissionLogicUseCase: SetPermissionLogicUseCase,
    private val grantActivityRecognitionPermissionUseCase: GrantActivityRecognitionPermissionUseCase
) : RequiresPermission(
    setPermissionLogicUseCase
) {
    fun activate(_permissionCallback: ((permissionStatus: Enum<PermissionStatus>) -> Unit)) {
        permissionCallback.unit = _permissionCallback
        grantActivityRecognitionPermissionUseCase()
    }
}