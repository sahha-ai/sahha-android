package sdk.sahha.android.domain.repository

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.health.connect.client.permission.Permission
import sdk.sahha.android.source.SahhaSensorStatus

interface PermissionsRepo {
    val healthConnectPermissions: Set<Permission>
    var healthConnectCallback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)?
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

    fun enableHealthConnect(
        context: Context,
        callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    )

    fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    )

    suspend fun getHealthConnectStatus(
        context: Context,
        callback: suspend ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    )

    fun containsAtleastOneGrantedPermission(granted: Set<Permission>): Boolean
}