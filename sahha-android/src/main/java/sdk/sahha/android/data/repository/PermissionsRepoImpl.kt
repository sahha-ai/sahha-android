package sdk.sahha.android.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import sdk.sahha.android.SahhaHealthConnectActivity
import sdk.sahha.android.SahhaPermissionActivity
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.domain.repository.PermissionsRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus

class PermissionsRepoImpl : PermissionsRepo {
    override val healthConnectPermissions = setOf(
        Permission.createReadPermission(HeartRateRecord::class),
        Permission.createWritePermission(HeartRateRecord::class),
        Permission.createReadPermission(StepsRecord::class),
        Permission.createWritePermission(StepsRecord::class),
        Permission.createReadPermission(SleepSessionRecord::class),
        Permission.createWritePermission(SleepSessionRecord::class)
    )
    private lateinit var permission: ActivityResultLauncher<String>
    override var healthConnectCallback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)? =
        null
    var healthConnectRequestCount = 0

    override fun setPermissionLogic(activity: ComponentActivity) {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
                val status = convertToActivityStatus(enabled)
                Sahha.motion.activityCallback.requestPermission?.let { it(null, status) }
            }
    }

    override fun openAppSettings(
        context: Context,
    ) {
        val openSettingsIntent = SahhaIntents.settings(context)
        context.startActivity(openSettingsIntent)
    }

    override fun activate(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val status = SahhaSensorStatus.unavailable
            callback(SahhaErrors.androidVersionTooLow(9), status)
            Sahha.motion.sensorStatus = status
            return
        }

        try {
            Sahha.motion.activityCallback.requestPermission = callback
            val intent = Intent(context, SahhaPermissionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            callback(e.message, SahhaSensorStatus.pending)
        }
    }

    private fun convertToActivityStatus(enabled: Boolean): Enum<SahhaSensorStatus> {
        if (enabled) return SahhaSensorStatus.enabled
        else return SahhaSensorStatus.disabled
    }

    override fun enableSensors(
        context: Context,
        callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    ) {
        SahhaPermissions.enableSensor(context) {
            callback(null, it)
            Sahha.start()
        }
    }

    override fun enableHealthConnect(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        healthConnectCallback = callback

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.startActivity(
                Intent(context, SahhaHealthConnectActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            healthConnectCallback?.invoke(
                SahhaErrors.androidVersionTooLow(10),
                SahhaSensorStatus.unavailable
            )
        }
    }

    override fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        SahhaPermissions.getSensorStatus(context) {
            callback(null, it)
        }
    }

    override suspend fun getHealthConnectStatus(
        context: Context,
        callback: suspend (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            callback(null, SahhaSensorStatus.unavailable)
            return
        }

        val client = AppModule.provideHealthConnectClient(context)

        client?.also {
            val granted = it.permissionController.getGrantedPermissions(healthConnectPermissions)

            if (containsAtleastOneGrantedPermission(granted)) {
                callback(null, SahhaSensorStatus.enabled)
            } else {
                ++healthConnectRequestCount
                if (healthConnectRequestCount > 2) {
                    callback(
                        SahhaErrors.healthConnect.permissionRequestDenied,
                        SahhaSensorStatus.disabled
                    )
                    return
                }
                callback(null, SahhaSensorStatus.disabled)
            }
        } ?: callback(null, SahhaSensorStatus.disabled)
    }

    override fun containsAtleastOneGrantedPermission(granted: Set<Permission>): Boolean {
        if (granted.isNotEmpty()) return true
        return false
    }
}