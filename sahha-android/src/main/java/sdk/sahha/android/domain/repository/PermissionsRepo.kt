package sdk.sahha.android.domain.repository

import android.content.Context
import androidx.activity.ComponentActivity
import sdk.sahha.android.source.SahhaActivityStatus

interface PermissionsRepo {
    fun setPermissionLogic(activity: ComponentActivity)
    fun promptUserToActivateActivityRecognition(
        context: Context,
        callback: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)
    )

    fun activate(callback: ((Enum<SahhaActivityStatus>) -> Unit))
}