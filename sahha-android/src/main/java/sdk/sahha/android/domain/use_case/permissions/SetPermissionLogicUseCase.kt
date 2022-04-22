package sdk.sahha.android.domain.use_case.permissions

import androidx.activity.ComponentActivity
import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class SetPermissionLogicUseCase @Inject constructor(
    private val repository: PermissionsRepo
) {
    operator fun invoke(activity: ComponentActivity) {
        repository.setPermissionLogic(activity)
    }
}