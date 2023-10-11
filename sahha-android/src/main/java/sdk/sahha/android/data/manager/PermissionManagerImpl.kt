package sdk.sahha.android.data.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import kotlinx.coroutines.CoroutineScope
import sdk.sahha.android.activity.SahhaPermissionActivity
import sdk.sahha.android.activity.health_connect.SahhaHealthConnectPermissionActivity
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.di.MainScope
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

private val tag = "PermissionManagerImpl"

class PermissionManagerImpl @Inject constructor(
    @MainScope private val mainScope: CoroutineScope,
    private val permissionHandler: PermissionHandler,
    private val healthConnectClient: HealthConnectClient?,
    private val sahhaErrorLogger: SahhaErrorLogger
) : PermissionManager {
    override var statusPending = true
    private lateinit var permission: ActivityResultLauncher<String>
    private val sim by lazy { Sahha.di.sahhaInteractionManager }

    override val shouldUseHealthConnect: Boolean
        get() {
            val isAndroid14AndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
//            return isAndroid14AndAbove
            return true
        }


    override val permissions =
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepStageRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(BloodGlucoseRecord::class),
            HealthPermission.getWritePermission(HeartRateRecord::class),
        )

    override fun <T : Activity> launchPermissionActivity(
        context: Context,
        activity: Class<T>
    ) {
        val intent = Intent(context, activity).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun enableNotifications(
        activity: AppCompatActivity,
        callback: ActivityResultCallback<Boolean>
    ) {
        val isTiramisuOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        if (!isTiramisuOrAbove) return

        val notificationPermission = Manifest.permission.POST_NOTIFICATIONS

        val contract = ActivityResultContracts.RequestPermission()
        val request = activity.registerForActivityResult(contract, callback)
        request.launch(notificationPermission)
    }

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
        checkAndEnable(
            context
        ) { _, _ ->
            if (shouldUseHealthConnect) {
                sim.startHealthConnect { _, _ ->
                    getSensorStatus(context, callback)
                }
                return@checkAndEnable
            }

            // Else start native sensors
            sim.startNative { _, _ ->
                getSensorStatus(context, callback)
            }
        }
    }

    private fun checkAndEnable(
        context: Context,
        callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    ) {
        if (shouldUseHealthConnect) {
            healthConnectClient?.also {
                permissionHandler.activityCallback.statusCallback = callback
                val intent = Intent(context, SahhaHealthConnectPermissionActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } ?: callback(SahhaErrors.somethingWentWrong, SahhaSensorStatus.unavailable)
            return
        }

        // Else use native sensors
        SahhaPermissions.enableSensor(context) { status ->
            callback(null, status)
        }
    }

    override fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        if (shouldUseHealthConnect) {
            SahhaPermissions.getSensorStatusHealthConnect {
                enabledTasks(it)
                callback(null, it)
            }
            return
        }

        // Else Native
        SahhaPermissions.getSensorStatus(context) {
            callback(null, it)
        }
    }

    private fun enabledTasks(status: Enum<SahhaSensorStatus>) {
        when (status) {
            SahhaSensorStatus.enabled -> sim.startHealthConnect()
        }
    }

    // Potentially usable in the future
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