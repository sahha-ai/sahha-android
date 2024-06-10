package sdk.sahha.android.domain.use_case.background

import android.content.Context
import sdk.sahha.android.domain.interaction.PermissionInteractionManager
import sdk.sahha.android.source.Sahha
import javax.inject.Inject

internal class RestartWorkers @Inject constructor(
    private val context: Context,
    private val pim: PermissionInteractionManager
) {
    suspend operator fun invoke(
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    ) {
        pim.startHcOrNativeDataCollection(
            context = context,
            callback = callback
        )
    }
}