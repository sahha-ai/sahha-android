package sdk.sahha.android.domain.repository

import android.content.Context
import androidx.activity.ComponentActivity
import sdk.sahha.android.source.SahhaActivityStatus

interface PermissionsRepo {
    fun setPermissionLogic(activity: ComponentActivity)
    fun openAppSettings(context: Context)
    fun activate(callback: ((error: String?, status: Enum<SahhaActivityStatus>) -> Unit))
}