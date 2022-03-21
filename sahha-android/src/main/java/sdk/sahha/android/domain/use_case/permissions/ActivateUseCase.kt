package sdk.sahha.android.domain.use_case.permissions

import sdk.sahha.android.domain.model.enums.ActivityStatus
import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class ActivateUseCase @Inject constructor(
    private val repository: PermissionsRepo
) {
    operator fun invoke(callback: ((Enum<ActivityStatus>) -> Unit)) {
        repository.activate(callback)
    }
}