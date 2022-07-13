package sdk.sahha.android.domain.use_case.permissions

import android.content.Context
import sdk.sahha.android.domain.repository.PermissionsRepo
import sdk.sahha.android.source.SahhaSensorStatus

class ActivateUseCase (
    private val repository: PermissionsRepo
) {
    operator fun invoke(context: Context, callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)) {
        repository.activate(context, callback)
    }
}