package sdk.sahha.android.data.manager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.HealthConnectClient
import sdk.sahha.android.activity.SahhaPermissionActivity
import sdk.sahha.android.activity.health_connect.SahhaHealthConnectPermissionActivity
import sdk.sahha.android.activity.health_connect.SahhaHealthConnectStatusActivity
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

private val tag = "PermissionManagerImpl"

class PermissionManagerImpl @Inject constructor(
    private val permissionHandler: PermissionHandler,
    private val healthConnectClient: HealthConnectClient?,
    private val sahhaErrorLogger: SahhaErrorLogger
) : PermissionManager {
    private lateinit var permission: ActivityResultLauncher<String>
    private val sim by lazy { Sahha.di.sahhaInteractionManager }

    override fun setPermissionLogic(activity: ComponentActivity) {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
                val status = convertToActivityStatus(enabled)
                permissionHandler.activityCallback.statusCallback?.let { it(null, status) }
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
            permissionHandler.sensorStatus = status
            return
        }

        try {
            permissionHandler.activityCallback.statusCallback = callback
            val intent = Intent(context, SahhaPermissionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            callback(e.message, SahhaSensorStatus.pending)

            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                tag,
                "activate",
                e.stackTraceToString()
            )
        }
    }

    private fun convertToActivityStatus(enabled: Boolean): Enum<SahhaSensorStatus> {
        return if (enabled) SahhaSensorStatus.enabled
        else SahhaSensorStatus.disabled
    }

    override fun enableSensors(
        context: Context,
        callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    ) {
        val androidSdkVersion = Build.VERSION.SDK_INT
        val isHealthConnectCompatible = checkHealthConnectCompatible(androidSdkVersion)
        val hasHealthConnectCompatibleAppInstalled = checkHealthConnectCompatibleAppIsInstalled(
            context,
        )
        val shouldUseHealthConnect =
            isHealthConnectCompatible && hasHealthConnectCompatibleAppInstalled
        checkAndEnable(
            context,
            shouldUseHealthConnect,
            callback
        )
    }

    private fun checkHealthConnectCompatible(androidSdkVersion: Int): Boolean {
        return androidSdkVersion >= 34
    }

    private fun checkAndEnable(
        context: Context,
        shouldUseHealthConnect: Boolean,
        callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    ) {
        if (shouldUseHealthConnect) {
            healthConnectClient?.also {
                permissionHandler.activityCallback.statusCallback = callback
                val intent = Intent(context, SahhaHealthConnectPermissionActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } ?: callback(SahhaErrors.somethingWentWrong, SahhaSensorStatus.unavailable)

//            sim.startHealthConnect()
            return
        }

        // Else use native sensors
        SahhaPermissions.enableSensor(context) { status ->
            callback(null, status)
            sim.startNative()
        }
    }

    override fun checkAndStart(
        context: Context,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        Sahha.di.permissionManager.getHealthConnectStatus(
            context = context,
        ) { err, status ->
            when (status) {
                SahhaSensorStatus.enabled -> Sahha.sim.startHealthConnect()
                else -> Sahha.sim.startNative()
            }

            err?.also { e ->
                callback?.invoke(e, false)
                sahhaErrorLogger.application(
                    e,
                    tag,
                    "checkAndStart",
                    status.name
                )
                return@getHealthConnectStatus
            }
            callback?.invoke(null, true)
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

    override fun getHealthConnectStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        val intent = Intent(context, SahhaHealthConnectStatusActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        permissionHandler.activityCallback.statusCallback = callback
        context.startActivity(intent)
    }

    private fun checkHealthConnectCompatibleAppIsInstalled(
        context: Context
    ): Boolean {
//        val compatibleApps = healthConnectRepo.getHealthConnectCompatibleApps()
//        compatibleApps.forEach { app ->
//            if (isAppInstalled(context, app.packageName)) return true
//        }
        return false
    }

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            val packageInfo = getPackageInfo(context, packageName)
            packageInfo.packageName == packageName
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getPackageInfo(context: Context, packageName: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(packageName, PackageInfoFlags.of(0))
        } else {
            context.packageManager.getPackageInfo(packageName, 0)
        }
    }
}