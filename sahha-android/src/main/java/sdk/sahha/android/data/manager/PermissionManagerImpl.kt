package sdk.sahha.android.data.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.health.connect.HealthConnectManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import kotlinx.coroutines.CoroutineScope
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.di.MainScope
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.framework.activity.SahhaPermissionActivity
import sdk.sahha.android.framework.activity.health_connect.SahhaHealthConnectPermissionActivity
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

private val tag = "PermissionManagerImpl"

class PermissionManagerImpl @Inject constructor(
    @MainScope private val mainScope: CoroutineScope,
    private val configRepo: SahhaConfigRepo,
    private val permissionHandler: PermissionHandler,
    private val healthConnectClient: HealthConnectClient?,
    private val sahhaErrorLogger: SahhaErrorLogger
) : PermissionManager {
    private lateinit var permission: ActivityResultLauncher<String>
    private val sim by lazy { Sahha.di.sahhaInteractionManager }

    override fun shouldUseHealthConnect(
        buildVersion: Int
    ): Boolean {
        val clientIsAvailable = healthConnectClient != null
        val isAndroid9OrAbove = buildVersion >= Build.VERSION_CODES.P

        return clientIsAvailable && isAndroid9OrAbove
    }


    override suspend fun getHcPermissions(): Set<String> {
        val permissions = mutableSetOf<String>()
        val enabledSensors = configRepo.getConfig().sensorArray

        if (enabledSensors.contains(SahhaSensor.sleep.ordinal)) {
            permissions.add(HealthPermission.getReadPermission(SleepStageRecord::class))
            permissions.add(HealthPermission.getReadPermission(SleepSessionRecord::class))
        }

        if (enabledSensors.contains(SahhaSensor.activity.ordinal)) {
            permissions.add(HealthPermission.getReadPermission(StepsRecord::class))
        }

        if (enabledSensors.contains(SahhaSensor.heart.ordinal)) {
            permissions.add(HealthPermission.getReadPermission(HeartRateRecord::class))
            permissions.add(HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class))
            permissions.add(HealthPermission.getReadPermission(RestingHeartRateRecord::class))
        }

        if (enabledSensors.contains(SahhaSensor.blood.ordinal)) {
            permissions.add(HealthPermission.getReadPermission(BloodPressureRecord::class))
            permissions.add(HealthPermission.getReadPermission(BloodGlucoseRecord::class))
        }

        if (enabledSensors.contains(SahhaSensor.oxygen.ordinal)) {
            permissions.add(HealthPermission.getReadPermission(Vo2MaxRecord::class))
            permissions.add(HealthPermission.getReadPermission(OxygenSaturationRecord::class))
            permissions.add(HealthPermission.getReadPermission(RespiratoryRateRecord::class))
        }

        if (enabledSensors.contains(SahhaSensor.energy.ordinal)) {
            permissions.add(HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class))
            permissions.add(HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class))
            permissions.add(HealthPermission.getReadPermission(BasalMetabolicRateRecord::class))
        }

        if (enabledSensors.contains(SahhaSensor.body.ordinal)) {
            permissions.add(HealthPermission.getReadPermission(HeightRecord::class))
            permissions.add(HealthPermission.getReadPermission(WeightRecord::class))
            permissions.add(HealthPermission.getReadPermission(LeanBodyMassRecord::class))
            permissions.add(HealthPermission.getReadPermission(BoneMassRecord::class))
            permissions.add(HealthPermission.getReadPermission(BodyWaterMassRecord::class))
            permissions.add(HealthPermission.getReadPermission(BodyFatRecord::class))
        }

        return permissions
    }


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

    override fun openHealthConnectSettings(context: Context) {
        val packageName = context.packageManager.getPackageInfo(context.packageName, 0).packageName
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS)
                .putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
        } else {
            Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
        }.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
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

    override fun requestNativeSensors(context: Context, callback: (status: Enum<SahhaSensorStatus>) -> Unit) {
        SahhaPermissions.enableSensor(context, callback)
    }

    override fun requestHealthConnectSensors(
        context: Context,
        callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    ) {
        healthConnectClient?.also {
            permissionHandler.activityCallback.statusCallback = callback
            val intent = Intent(context, SahhaHealthConnectPermissionActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } ?: callback(SahhaErrors.noHealthConnectApp, SahhaSensorStatus.unavailable)
    }

    override fun getHealthConnectSensorStatus(callback: ((status: Enum<SahhaSensorStatus>) -> Unit)) {
        SahhaPermissions.getSensorStatusHealthConnect(callback)
    }

    override fun getNativeSensorStatus(context: Context, callback: ((status: Enum<SahhaSensorStatus>) -> Unit)) {
        SahhaPermissions.getSensorStatus(context, callback)
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

