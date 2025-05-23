package sdk.sahha.android.domain.use_case.permissions

import android.content.Context
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

internal class ActivateUseCase @Inject constructor (
    private val repository: PermissionManager
) {
    suspend operator fun invoke(context: Context, callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)) {
        repository.activate(context, callback)
    }
}