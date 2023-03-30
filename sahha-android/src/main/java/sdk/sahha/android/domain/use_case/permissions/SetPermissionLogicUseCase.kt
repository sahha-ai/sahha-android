package sdk.sahha.android.domain.use_case.permissions

import androidx.activity.ComponentActivity
import sdk.sahha.android.domain.manager.PermissionManager

class SetPermissionLogicUseCase (
    private val repository: PermissionManager
) {
    operator fun invoke(activity: ComponentActivity) {
        repository.setPermissionLogic(activity)
    }
}