package sdk.sahha.android.domain.use_case.permissions

import androidx.activity.ComponentActivity
import sdk.sahha.android.domain.manager.PermissionManager
import javax.inject.Inject

internal class SetPermissionLogicUseCase @Inject constructor (
    private val repository: PermissionManager
) {
    operator fun invoke(activity: ComponentActivity) {
        repository.setPermissionLogic(activity)
    }
}