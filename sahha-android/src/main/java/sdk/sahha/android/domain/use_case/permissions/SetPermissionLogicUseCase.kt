package sdk.sahha.android.domain.use_case.permissions

import sdk.sahha.android.domain.model.PermissionCallback
import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class SetPermissionLogicUseCase @Inject constructor(
    private val repository: PermissionsRepo
) {
    operator fun invoke(permissionCallback: PermissionCallback) {
        repository.setPermissionLogic(permissionCallback)
    }
}