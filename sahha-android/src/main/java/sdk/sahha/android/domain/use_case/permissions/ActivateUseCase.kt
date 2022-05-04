package sdk.sahha.android.domain.use_case.permissions

import android.content.Context
import sdk.sahha.android.source.SahhaActivityStatus
import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class ActivateUseCase @Inject constructor(
    private val repository: PermissionsRepo
) {
    operator fun invoke(callback: ((error: String?, status: Enum<SahhaActivityStatus>) -> Unit)) {
        repository.activate(callback)
    }

    fun testNewActivate(context: Context) {
        repository.testNewActivate(context)
    }
}