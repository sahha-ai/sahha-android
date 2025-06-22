package sdk.sahha.android.data.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.data.local.dao.ManualPermissionsDao
import sdk.sahha.android.di.MainScope
import sdk.sahha.android.domain.internal_enum.InternalSensorStatus
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.domain.model.permissions.ManualPermission
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.framework.activity.SahhaPermissionActivity
import sdk.sahha.android.framework.activity.health_connect.SahhaHealthConnectPermissionActivity
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

private val tag = "PermissionManagerImpl"

internal class PermissionManagerImpl @Inject constructor(
    @MainScope private val mainScope: CoroutineScope,
    private val configRepo: SahhaConfigRepo,
    private val manualPermissionsDao: ManualPermissionsDao,
    private val permissionHandler: PermissionHandler,
    private val healthConnectClient: HealthConnectClient?,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val sharedPrefs: SharedPreferences
) : PermissionManager {
    private lateinit var permission: ActivityResultLauncher<String>
    private val sim by lazy { Sahha.di.sahhaInteractionManager }

    override val isFirstHealthConnectRequest
        get() = sharedPrefs.getBoolean(Constants.FIRST_HC_REQUEST_KEY, true)

    override fun isFirstHealthConnectRequest(firstRequest: Boolean) {
        sharedPrefs.edit().putBoolean(Constants.FIRST_HC_REQUEST_KEY, firstRequest).apply()
    }

    override fun shouldUseHealthConnect(
        buildVersion: Int
    ): Boolean {
        val clientIsAvailable = healthConnectClient != null
        val isAndroid9OrAbove = buildVersion >= Build.VERSION_CODES.P

        return clientIsAvailable && isAndroid9OrAbove
    }

    override suspend fun getTrimmedHcPermissions(
        manifestPermissions: Set<String>?,
        sensors: Set<SahhaSensor>,
        callback: (suspend (error: String?, status: Enum<SahhaSensorStatus>?, permissions: Set<String>) -> Unit)?
    ) {
        val permissions = getHcPermissions(sensors)

        manifestPermissions?.also { mPermissions ->
            if (mPermissions.isEmpty()) {
                callback?.invoke(null, null, permissions)
                return@also
            }

            logUndeclaredPermissions(permissions, mPermissions) { error, status ->
                callback?.invoke(error, status, permissions)
            }
        } ?: callback?.invoke(null, null, permissions)
    }

    private suspend fun logUndeclaredPermissions(
        permissions: Set<String>,
        manifestPermissions: Set<String>,
        callback: (suspend (error: String?, status: Enum<SahhaSensorStatus>?) -> Unit)?
    ) {
        var undeclared = ""
        permissions.forEach { permission ->
            if (!manifestPermissions.contains(permission)) {
                val error = "Permission: [$permission] is not declared in the AndroidManifest!"
                undeclared += "$error\n"
                Log.e(tag, error)
            }
        }

        if (undeclared.isNotEmpty()) {
            callback?.invoke(undeclared, SahhaSensorStatus.unavailable)
            return
        }

        // Else
        callback?.invoke(null, null)
    }

    override suspend fun getManifestPermissions(context: Context): Set<String>? {
        return try {
            val packageName = context.packageName
            val packageInfo =
                context.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val requestedPermissions = packageInfo.requestedPermissions
            requestedPermissions?.toSet()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }


    private fun getHcPermissions(
        sensors: Set<SahhaSensor>
    ): Set<String> {
        val permissions = mutableSetOf<String>()

        if (sensors.contains(SahhaSensor.sleep)) permissions.add(
            HealthPermission.getReadPermission(
                SleepSessionRecord::class
            )
        )

        if (sensors.contains(SahhaSensor.steps)) permissions.add(
            HealthPermission.getReadPermission(StepsRecord::class)
        )

        if (sensors.contains(SahhaSensor.floors_climbed)) permissions.add(
            HealthPermission.getReadPermission(FloorsClimbedRecord::class)
        )

        if (sensors.contains(SahhaSensor.heart_rate)) permissions.add(
            HealthPermission.getReadPermission(HeartRateRecord::class)
        )

        if (sensors.contains(SahhaSensor.heart_rate_variability_rmssd)) permissions.add(
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class)
        )

        if (sensors.contains(SahhaSensor.resting_heart_rate)) permissions.add(
            HealthPermission.getReadPermission(RestingHeartRateRecord::class)
        )

        if (sensors.contains(SahhaSensor.blood_pressure_diastolic)) permissions.add(
            HealthPermission.getReadPermission(BloodPressureRecord::class)
        )

        if (sensors.contains(SahhaSensor.blood_pressure_systolic)) permissions.add(
            HealthPermission.getReadPermission(BloodPressureRecord::class)
        )

        if (sensors.contains(SahhaSensor.blood_glucose)) permissions.add(
            HealthPermission.getReadPermission(BloodGlucoseRecord::class)
        )

        if (sensors.contains(SahhaSensor.oxygen_saturation)) permissions.add(
            HealthPermission.getReadPermission(OxygenSaturationRecord::class)
        )

        if (sensors.contains(SahhaSensor.vo2_max)) permissions.add(
            HealthPermission.getReadPermission(
                Vo2MaxRecord::class
            )
        )

        if (sensors.contains(SahhaSensor.respiratory_rate)) permissions.add(
            HealthPermission.getReadPermission(RespiratoryRateRecord::class)
        )

        if (sensors.contains(SahhaSensor.active_energy_burned)) permissions.add(
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
        )

        if (sensors.contains(SahhaSensor.total_energy_burned)) permissions.add(
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
        )

        if (sensors.contains(SahhaSensor.basal_metabolic_rate)) permissions.add(
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class)
        )

        if (sensors.contains(SahhaSensor.height)) permissions.add(
            HealthPermission.getReadPermission(
                HeightRecord::class
            )
        )

        if (sensors.contains(SahhaSensor.weight)) permissions.add(
            HealthPermission.getReadPermission(
                WeightRecord::class
            )
        )

        if (sensors.contains(SahhaSensor.lean_body_mass)) permissions.add(
            HealthPermission.getReadPermission(LeanBodyMassRecord::class)
        )

        if (sensors.contains(SahhaSensor.bone_mass)) permissions.add(
            HealthPermission.getReadPermission(BoneMassRecord::class)
        )

        if (sensors.contains(SahhaSensor.body_water_mass)) permissions.add(
            HealthPermission.getReadPermission(BodyWaterMassRecord::class)
        )

        if (sensors.contains(SahhaSensor.body_fat)) permissions.add(
            HealthPermission.getReadPermission(BodyFatRecord::class)
        )

        if (sensors.contains(SahhaSensor.body_temperature)) permissions.add(
            HealthPermission.getReadPermission(BodyTemperatureRecord::class)
        )

        if (sensors.contains(SahhaSensor.basal_body_temperature)) permissions.add(
            HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class)
        )

        if (sensors.contains(SahhaSensor.exercise)) permissions.add(
            HealthPermission.getReadPermission(ExerciseSessionRecord::class)
        )

        if(sensors.contains(SahhaSensor.energy_consumed)) permissions.add(
            HealthPermission.getReadPermission(NutritionRecord::class)
        )

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

                activity.lifecycleScope.launch {
                    permissionHandler.activityCallback.statusCallback?.invoke(null, status)
                }
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
        val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

//        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS)
//                .putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
//        } else {
//            Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
//        }.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }

    override suspend fun activate(
        context: Context,
        callback: (suspend (error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) = coroutineScope {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val status = SahhaSensorStatus.unavailable
            callback(SahhaErrors.androidVersionTooLow(9), status)
            permissionHandler.sensorStatus = status
            return@coroutineScope
        }

        try {
            permissionHandler.activityCallback.statusCallback = callback
            val intent = Intent(context, SahhaPermissionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            callback(e.message, SahhaSensorStatus.pending)

            sahhaErrorLogger.application(
                message = e.message ?: SahhaErrors.somethingWentWrong,
                path = tag,
                method = "activate",
                body = e.stackTraceToString()
            )
        }
    }

    private fun convertToActivityStatus(enabled: Boolean): Enum<SahhaSensorStatus> {
        return if (enabled) SahhaSensorStatus.enabled
        else SahhaSensorStatus.disabled
    }

    override fun requestNativeSensors(
        context: Context,
        callback: (status: Enum<SahhaSensorStatus>) -> Unit
    ) {
        SahhaPermissions.enableSensor(context, callback)
    }

    override suspend fun requestHealthConnectSensors(
        context: Context,
        callback: suspend (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    ) {
        healthConnectClient?.also {
            permissionHandler.activityCallback.statusCallback = callback
            val intent = Intent(context, SahhaHealthConnectPermissionActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } ?: callback(SahhaErrors.noHealthConnectApp, SahhaSensorStatus.unavailable)
    }

    override suspend fun getDeviceOnlySensorStatus(callback: ((status: Enum<InternalSensorStatus>) -> Unit)) {
        val permission = manualPermissionsDao.getPermissionStatus(SahhaSensor.device_lock.ordinal)
        val status = InternalSensorStatus.values().find { it.ordinal == permission?.statusEnum }
        callback(status ?: InternalSensorStatus.pending)
    }

    override suspend fun enableDeviceOnlySensor(callback: ((status: Enum<SahhaSensorStatus>) -> Unit)?) {
        manualPermissionsDao.savePermission(
            ManualPermission(
                SahhaSensor.device_lock.ordinal,
                SahhaSensorStatus.enabled.ordinal
            )
        )
        callback?.invoke(SahhaSensorStatus.enabled)
    }

    override suspend fun getHealthConnectSensorStatus(
        context: Context,
        sensors: Set<SahhaSensor>,
        callback: suspend ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        SahhaPermissions.getSensorStatusHealthConnect(context, sensors, callback)
    }

    override fun getNativeSensorStatus(
        context: Context,
        callback: ((status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
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

