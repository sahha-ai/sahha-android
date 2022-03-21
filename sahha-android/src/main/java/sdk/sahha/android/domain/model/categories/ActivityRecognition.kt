package sdk.sahha.android.domain.model.categories

import sdk.sahha.android.domain.model.enums.ActivityStatus
import sdk.sahha.android.domain.use_case.permissions.ActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.PromptUserToActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import javax.inject.Inject

class ActivityRecognition @Inject constructor(
    setPermissionLogicUseCase: SetPermissionLogicUseCase,
    private val activateUseCase: ActivateUseCase,
    private val promptUserToActivateUseCase: PromptUserToActivateUseCase
) : RequiresPermission(
    setPermissionLogicUseCase
) {
    fun activate(_activityCallback: ((activityStatus: Enum<ActivityStatus>) -> Unit)) {
        activityCallback.requestPermission = _activityCallback

    }

    fun promptUserToActivate(_activityCallback: ((activityStatus: Enum<ActivityStatus>) -> Unit)) {
        activityCallback.requestPermission = _activityCallback
        promptUserToActivateUseCase(_activityCallback)
    }
}