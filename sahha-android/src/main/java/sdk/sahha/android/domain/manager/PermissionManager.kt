package sdk.sahha.android.domain.manager

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.appcompat.app.AppCompatActivity
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

    val permissions: Set<String>
    var statusPending: Boolean
    val shouldUseHealthConnect: Boolean
    fun <T: Activity> launchPermissionActivity(context: Context, activity: Class<T>)
    fun enableNotifications(activity: AppCompatActivity, callback: ActivityResultCallback<Boolean>)
}