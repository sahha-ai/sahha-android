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
import kotlinx.coroutines.launch
import sdk.sahha.android.activity.SahhaPermissionActivity
import sdk.sahha.android.activity.health_connect.SahhaHealthConnectPermissionActivity
import sdk.sahha.android.activity.health_connect.SahhaHealthConnectStatusActivity
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val tag = "PermissionManagerImpl"

class PermissionManagerImpl @Inject constructor(
    @DefaultScope private val defaultScope: CoroutineScope,
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
            return isAndroid14AndAbove
//            return true
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
        )

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
                sim.startHealthConnect { error, success ->
                    callback(
                        error,
                        if (success) SahhaSensorStatus.enabled
                        else SahhaSensorStatus.disabled
                    )
                }
                return@checkAndEnable
            }

            // Else start native sensors
            sim.startNative { error, success ->
                callback(
                    error,
                    if (success) SahhaSensorStatus.enabled
                    else SahhaSensorStatus.disabled
                )
            }
        }
    }

    private fun checkHealthConnectCompatible(androidSdkVersion: Int): Boolean {
        return androidSdkVersion >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
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

    override suspend fun checkAndStart(
        context: Context,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        println("checkAndStart0000")
        val status = awaitStatus(context)
        println("checkAndStart0001")
        if (status == SahhaSensorStatus.enabled) {
            println("checkAndStart0002")
            Sahha.sim.startHealthConnect(callback)
        }

//        when (awaitStatus(context)) {
//            SahhaSensorStatus.enabled -> {
//                println("checkAndStart0002")
//                Sahha.sim.startHealthConnect(callback)
//            }
//
//            SahhaSensorStatus.pending -> {
//                println("checkAndStart0005")
//            }
//
//            SahhaSensorStatus.disabled -> {
//                println("checkAndStart0004")
//            }
//
//            SahhaSensorStatus.unavailable -> {
////                    Sahha.sim.startNative(callback)
//                println("checkAndStart0003")
//                Sahha.sim.startHealthConnect(callback)
//            }
//
//            else -> println("checkAndStart0006")
//        }
    }

    private suspend fun awaitStatus(context: Context): Enum<SahhaSensorStatus>? {
        return suspendCoroutine { cont ->
            getHealthConnectStatus(
                context = context,
            ) { err, status ->
                println("awaitStatus")
                err?.also { e ->
                    cont.resume(null)
                    sahhaErrorLogger.application(
                        e,
                        tag,
                        "checkAndStart",
                        status.name
                    )
                    return@getHealthConnectStatus
                }

                cont.resume(status)
//            testStartNativeAndHc(callback)
            }
        }
    }

    // Tester method
    private fun testStartNativeAndHc(
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        Sahha.sim.startNative { error, success ->
            Sahha.sim.startHealthConnect(callback)
        }
    }

    override fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        defaultScope.launch {
            if (shouldUseHealthConnect) {
                val status = awaitStatus()
                enabledTasks(status)
                callback(null, status)
                return@launch
            }

            // Else Native
            SahhaPermissions.getSensorStatus(context) {
                callback(null, it)
            }
        }

    }

    private fun enabledTasks(status: Enum<SahhaSensorStatus>) {
        when(status) {
            SahhaSensorStatus.enabled -> Sahha.sim.startHealthConnect()
        }
    }

    private suspend fun awaitStatus(): Enum<SahhaSensorStatus> = suspendCoroutine { cont ->
        SahhaPermissions.getSensorStatusHealthConnect { status ->
            cont.resume(status)
        }
    }

    override fun getHealthConnectStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        permissionHandler.activityCallback.statusCallback = callback
        val intent = Intent(context, SahhaHealthConnectStatusActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

//    private fun checkHealthConnectCompatibleAppIsInstalled(
//        context: Context
//    ): Boolean {
//        val compatibleApps = healthConnectRepo.getHealthConnectCompatibleApps()
//        compatibleApps.forEach { app ->
//            if (isAppInstalled(context, app.packageName)) return true
//        }
//        return false
//    }

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