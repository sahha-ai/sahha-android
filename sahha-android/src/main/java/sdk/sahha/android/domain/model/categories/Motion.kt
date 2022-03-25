package sdk.sahha.android.domain.model.categories

import sdk.sahha.android.domain.model.enums.SahhaActivityStatus
import sdk.sahha.android.domain.use_case.permissions.ActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.PromptUserToActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import javax.inject.Inject

class Motion @Inject constructor(
    setPermissionLogicUseCase: SetPermissionLogicUseCase,
    private val activateUseCase: ActivateUseCase,
    private val promptUserToActivateUseCase: PromptUserToActivateUseCase
) : RequiresPermission(
    setPermissionLogicUseCase
) {
    fun activate(_activityCallback: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)) {
        activateUseCase(_activityCallback)
    }

    fun promptUserToActivate(_activityCallback: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)) {
        promptUserToActivateUseCase(_activityCallback)
    }
}