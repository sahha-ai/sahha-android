package sdk.sahha.android.domain.interaction

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.di.MainScope
import sdk.sahha.android.domain.manager.SahhaAlarmManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.framework.activity.SahhaNotificationPermissionActivity
import sdk.sahha.android.framework.receiver.BackgroundTaskRestarterReceiver
import sdk.sahha.android.framework.service.HealthConnectPostService
import sdk.sahha.android.source.SahhaFramework
import sdk.sahha.android.source.SahhaNotificationConfiguration
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.coroutines.resume

private const val tag = "SahhaInteractionManager"

internal class SahhaInteractionManager @Inject constructor(
    @MainScope private val mainScope: CoroutineScope,
    @DefaultScope private val defaultScope: CoroutineScope,
    internal val auth: AuthInteractionManager,
    internal val permission: PermissionInteractionManager,
    internal val userData: UserDataInteractionManager,
    internal val sensor: SensorInteractionManager,
    internal val insights: InsightsInteractionManager,
    internal val notifications: SahhaNotificationManager,
    private val alarms: SahhaAlarmManager,
    private val sahhaConfigRepo: SahhaConfigRepo,
    private val sensorRepo: SensorRepo,
    private val sahhaErrorLogger: SahhaErrorLogger,
) {
    internal suspend fun configure(
        application: Application,
        sahhaSettings: SahhaSettings,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        saveConfiguration(sahhaSettings)

        auth.migrateDataIfNeeded { error, success ->
            if (!success) {
                callback?.invoke(error, false)
                return@migrateDataIfNeeded
            }

            defaultScope.launch {
                listOf(
                    async { saveNotificationConfig(sahhaSettings.notificationSettings) },
                ).joinAll()

                awaitProcessAndPutDeviceInfo(application)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    permission.manager.launchPermissionActivity(
                        application,
                        SahhaNotificationPermissionActivity::class.java,
                    )

                permission.startHcOrNativeDataCollection(application, callback)
            }
        }
    }

    fun scheduleInsightsAlarm(
        context: Context,
        localTime: LocalTime = LocalTime.of(Constants.ALARM_6PM, 5)
    ) {
        val insightsPendingIntent = alarms.getInsightsQueryPendingIntent(context)
        val timestamp =
            ZonedDateTime.of(
                LocalDate.now(),
                localTime,
                ZonedDateTime.now().offset
            )

        alarms.setAlarm(insightsPendingIntent, timestamp.toInstant().toEpochMilli())
    }

    fun scheduleBackgroundTaskRestarter(
        context: Context,
        nextAlarmTime: LocalDateTime =
            LocalDateTime.of(
                LocalDate.now().plusDays(1),
                LocalTime.of(Constants.ALARM_12AM, 0),
            )
    ) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Constants.RESTARTER_RECEIVER,
            Intent(context, BackgroundTaskRestarterReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextAlarmTimeEpochMillis = ZonedDateTime.of(
            nextAlarmTime,
            ZonedDateTime.now().offset
        ).toInstant().toEpochMilli()

        alarms.setAlarm(
            pendingIntent = pendingIntent,
            setTimeEpochMillis = nextAlarmTimeEpochMillis
        )
    }

    internal fun requestNotificationPermission(
        context: Context
    ) = mainScope.launch {
        permission.manager.launchPermissionActivity(
            context,
            SahhaNotificationPermissionActivity::class.java
        )
    }

    private suspend fun awaitProcessAndPutDeviceInfo(context: Context) =
        suspendCancellableCoroutine { cont ->
            defaultScope.launch {
                userData.processAndPutDeviceInfo(context) { _, success ->
                    if (cont.isActive) cont.resume(success)
                }
            }
        }

    internal fun startNative(
        context: Context,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        try {
            defaultScope.launch {
                alarms.stopAllAlarms(context)
                sensorRepo.stopAllWorkers()
                sensor.unregisterExistingReceiversAndListeners(context.applicationContext)
                listOf(
                    async { sensor.startDataCollection(context) },
                    async { sensor.checkAndStartPostWorkers(context) },
                    async { scheduleBackgroundTaskRestarter(context) }
                ).joinAll()

                callback?.invoke(null, true)
            }
        } catch (e: Exception) {
            callback?.invoke("Error: ${e.message}", false)
            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                tag,
                "startNative"
            )
        }
    }

    internal fun startNativeAndHealthConnect(
        context: Context,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        try {
            defaultScope.launch {
                alarms.stopAllAlarms(context)
                sensorRepo.stopAllWorkers()
                sensor.unregisterExistingReceiversAndListeners(context.applicationContext)
                listOf(
                    async { sensor.startDataCollection(context) },
                    async { sensor.checkAndStartPostWorkers(context) },
                    async { notifications.startForegroundService(HealthConnectPostService::class.java) },
                    async { scheduleInsightsAlarm(context) },
                    async { scheduleBackgroundTaskRestarter(context) }
                ).joinAll()

                callback?.invoke(null, true)
            }
        } catch (e: Exception) {
            callback?.invoke("Error: ${e.message}", false)
            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                tag,
                "startNativeAndHealthConnect"
            )
        }
    }

    internal fun postAppError(
        framework: SahhaFramework,
        message: String,
        path: String,
        method: String,
        body: String? = null,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        try {
            sahhaErrorLogger.application(
                message, path, method, body, framework, callback
            )
        } catch (e: Exception) {
            callback?.invoke("Error: ${e.message}", false)
            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                path,
                "postAppError",
                framework = framework
            )
        }
    }

    private suspend fun saveConfiguration(
        settings: SahhaSettings
    ) {
        val sensorEnums = settings.sensors?.let {
            convertToEnums(it)
        } ?: convertToEnums(SahhaSensor.values().toSet())

        sahhaConfigRepo.saveConfig(
            SahhaConfiguration(
                settings.environment.ordinal,
                settings.framework.name,
                sensorEnums,
                false
            )
        )
    }

    private suspend fun saveNotificationConfig(config: SahhaNotificationConfiguration?) {
        sahhaConfigRepo.saveNotificationConfig(
            config ?: SahhaNotificationConfiguration()
        )
    }

    private fun convertToEnums(sensorSet: Set<Enum<SahhaSensor>>): ArrayList<Int> {
        val sensorEnums = arrayListOf<Int>()
        sensorSet.forEach {
            sensorEnums.add(it.ordinal)
        }
        return sensorEnums
    }
}