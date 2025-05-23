package sdk.sahha.android.domain.manager

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.appcompat.app.AppCompatActivity
import sdk.sahha.android.domain.internal_enum.InternalSensorStatus
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus

internal interface PermissionManager {
    fun setPermissionLogic(activity: ComponentActivity)
    fun openAppSettings(context: Context)
    suspend fun activate(
        context: Context,
        callback: suspend (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    )
    suspend fun getManifestPermissions(context: Context): Set<String>?
    fun <T: Activity> launchPermissionActivity(context: Context, activity: Class<T>)
    fun enableNotifications(activity: AppCompatActivity, callback: ActivityResultCallback<Boolean>)
    fun shouldUseHealthConnect(buildVersion: Int = Build.VERSION.SDK_INT): Boolean
    fun requestNativeSensors(context: Context, callback: (status: Enum<SahhaSensorStatus>) -> Unit)

    suspend fun requestHealthConnectSensors(
        context: Context,
        callback: suspend (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    )
    fun getNativeSensorStatus(context: Context, callback: (status: Enum<SahhaSensorStatus>) -> Unit)
    fun openHealthConnectSettings(context: Context)
    suspend fun getDeviceOnlySensorStatus(callback: (status: Enum<InternalSensorStatus>) -> Unit)

    suspend fun enableDeviceOnlySensor(callback: ((status: Enum<SahhaSensorStatus>) -> Unit)? = null)
    suspend fun getHealthConnectSensorStatus(
        context: Context,
        sensors: Set<SahhaSensor>,
        callback: suspend (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    )
    suspend fun getTrimmedHcPermissions(
        manifestPermissions: Set<String>?,
        sensors: Set<SahhaSensor>,
        callback: (suspend (error: String?, status: Enum<SahhaSensorStatus>?, permissions: Set<String>) -> Unit)?
    )
    fun isFirstHealthConnectRequest(firstRequest: Boolean)
    val isFirstHealthConnectRequest: Boolean
}