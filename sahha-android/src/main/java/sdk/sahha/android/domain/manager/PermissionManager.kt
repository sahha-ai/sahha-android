package sdk.sahha.android.domain.manager

import android.content.Context
import androidx.activity.ComponentActivity
import sdk.sahha.android.source.SahhaSensorStatus

interface PermissionManager {
    fun setPermissionLogic(activity: ComponentActivity)
    fun openAppSettings(context: Context)
    fun activate(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    )

    fun enableSensors(
        context: Context,
        callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    )

    fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    )

    fun getHealthConnectStatus(
        context: Context,
        callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    )

    fun checkAndStart(
        context: Context,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    )
}