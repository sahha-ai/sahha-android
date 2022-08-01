package sdk.sahha.android.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import sdk.sahha.android.SahhaPermissionActivity
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.repository.PermissionsRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PermissionsRepoImpl : PermissionsRepo {
    private lateinit var permission: ActivityResultLauncher<String>

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
        sensors: Set<SahhaSensor>,
        callback: (error: String?, statuses: Map<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>) -> Unit
    ) = Sahha.di.mainScope.launch {
        val statuses = suspendCoroutine<Map<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>> { cont ->
            launch {
                val map = enableAndGetMapOfSensorStatuses(context, sensors)
                cont.resume(map)
            }
        }

        launch {
            Sahha.di.configurationDao.updateConfig(convertToArrayList(sensors))
            Sahha.start()
            callback(null, statuses)
        }
    }

    override fun getSensorStatuses(
        context: Context,
        sensors: Set<SahhaSensor>,
        callback: ((error: String?, statuses: Map<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>) -> Unit)
    ) = Sahha.di.mainScope.launch {
        val sharedPrefs =
            context.getSharedPreferences(Constants.SENSOR_SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val statuses = suspendCoroutine<Map<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>> { cont ->
            launch {
                val map = getMapOfSensorStatuses(context, sharedPrefs, sensors)
                cont.resume(map)
            }
        }
        callback(null, statuses)
    }

    private fun tryGetSensorStatus(
        sharedPrefs: SharedPreferences,
        sensor: SahhaSensor
    ): SahhaSensorStatus {
        return SahhaSensorStatus.valueOf(
            sharedPrefs.getString(
                sensor.name,
                SahhaSensorStatus.pending.name
            ) ?: SahhaSensorStatus.pending.name
        )
    }

    private fun enableAndStoreDeviceSensorStatus(
        context: Context,
        sensor: SahhaSensor
    ): Enum<SahhaSensorStatus> {
        val sharedPrefs =
            context.getSharedPreferences(Constants.SENSOR_SHARED_PREF_KEY, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(sensor.name, SahhaSensorStatus.enabled.name).apply()
        return SahhaSensorStatus.enabled
    }

    private suspend fun awaitGetSensorStatus(
        context: Context,
        sensor: SahhaSensor
    ): Pair<Enum<SahhaSensor>, Enum<SahhaSensorStatus>> {
        return suspendCoroutine<Pair<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>> { statusCont ->
            SahhaPermissions.getSensorStatus(context, sensor) { sensorStatus ->
                statusCont.resume(Pair(sensor, sensorStatus))
            }
        }
    }

    private suspend fun awaitEnableSensorStatus(
        context: Context,
        sensor: SahhaSensor
    ): Pair<Enum<SahhaSensor>, Enum<SahhaSensorStatus>> {
        return suspendCoroutine<Pair<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>> { statusCont ->
            SahhaPermissions.enableSensor(context, sensor) { sensorStatus ->
                statusCont.resume(Pair(sensor, sensorStatus))
            }
        }
    }

    private suspend fun enableAndGetMapOfSensorStatuses(
        context: Context,
        sensors: Set<SahhaSensor>
    ): Map<Enum<SahhaSensor>, Enum<SahhaSensorStatus>> {
        val map = mutableMapOf<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>()
        sensors.forEach { sensor ->
            if (sensor == SahhaSensor.device) {
                map[sensor] = enableAndStoreDeviceSensorStatus(context, sensor)
            } else {
                val awaitEnableStatus = awaitEnableSensorStatus(context, sensor)
                map[awaitEnableStatus.first] = awaitEnableStatus.second
            }
        }
        return map
    }

    private suspend fun getMapOfSensorStatuses(
        context: Context,
        sharedPrefs: SharedPreferences,
        sensors: Set<SahhaSensor>
    ): Map<Enum<SahhaSensor>, Enum<SahhaSensorStatus>> {
        val map = mutableMapOf<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>()
        sensors.forEach { sensor ->
            if (sensor == SahhaSensor.device) {
                if (!sharedPrefs.contains(sensor.name))
                    map[sensor] = SahhaSensorStatus.pending
                else map[sensor] = tryGetSensorStatus(sharedPrefs, sensor)
            } else {
                val awaitStatus = awaitGetSensorStatus(context, sensor)
                map[awaitStatus.first] = awaitStatus.second
            }
        }
        return map
    }

    private fun convertToArrayList(sensors: Set<SahhaSensor>): ArrayList<Int> {
        val sensorsArrayList: ArrayList<Int> = arrayListOf()
        sensors.mapTo(sensorsArrayList) { it.ordinal }
        return sensorsArrayList
    }
}