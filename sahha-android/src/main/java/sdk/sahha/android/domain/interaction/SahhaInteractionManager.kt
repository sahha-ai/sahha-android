package sdk.sahha.android.domain.interaction

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.activity.SahhaNotificationPermissionActivity
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.di.MainScope
import sdk.sahha.android.domain.manager.SahhaAlarmManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaFramework
import sdk.sahha.android.source.SahhaNotificationConfiguration
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings
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
    internal val notifications: SahhaNotificationManager,
    private val sahhaConfigRepo: SahhaConfigRepo,
    private val sensorRepo: SensorRepo,
    private val sahhaAlarmManager: SahhaAlarmManager,
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
                Sahha.config = sahhaConfigRepo.getConfig()

                listOf(
                    async { saveNotificationConfig(sahhaSettings.notificationSettings) },
                ).joinAll()

                awaitProcessAndPutDeviceInfo(application)
                permission.manager.launchPermissionActivity(
                    application,
                    SahhaNotificationPermissionActivity::class.java,
                )

                permission.startHcOrNativeDataCollection(application, callback)
            }
        }
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

    internal fun startNative(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        try {
            defaultScope.launch {
                sahhaAlarmManager.stopAlarm(sahhaAlarmManager.pendingIntent)
                sensorRepo.stopAllWorkers()
                Sahha.config = sahhaConfigRepo.getConfig()
                listOf(
                    async { sensor.startDataCollection() },
                    async { sensor.checkAndStartPostWorkers() },
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

    internal fun startHealthConnect(
        context: Context,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        try {
            defaultScope.launch {
                sensorRepo.stopAllWorkers()
                sensor.unregisterExistingReceiversAndListeners(context.applicationContext)
                Sahha.config = sahhaConfigRepo.getConfig()
                notifications.startDataCollectionService { _, _ ->
                    sensor.checkAndStartDevicePostWorker(callback)
                    notifications.startHealthConnectPostService()
                }
            }
        } catch (e: Exception) {
            callback?.invoke("Error: ${e.message}", false)
            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                tag,
                "startHealthConnect"
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