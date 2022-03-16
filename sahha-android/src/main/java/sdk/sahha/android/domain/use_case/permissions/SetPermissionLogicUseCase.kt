package sdk.sahha.android.domain.use_case.permissions

import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class SetPermissionLogicUseCase @Inject constructor(
    private val repository: PermissionsRepo
) {
    operator fun invoke(logic: ((enabled: Boolean) -> Unit)) {
        repository.setPermissionLogic(logic)
    }
}