package sdk.sahha.android.domain.repository

import android.content.Context
import androidx.activity.ComponentActivity
import sdk.sahha.android.source.SahhaSensorStatus

interface PermissionsRepo {
    fun setPermissionLogic(activity: ComponentActivity)
    fun openAppSettings(context: Context)
    fun activate(context: Context, callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit))
}