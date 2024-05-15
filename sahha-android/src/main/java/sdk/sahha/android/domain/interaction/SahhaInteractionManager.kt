package sdk.sahha.android.domain.interaction

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.Session
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.di.MainScope
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.framework.activity.SahhaNotificationPermissionActivity
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
    internal val insights: InsightsInteractionManager,
    internal val notifications: SahhaNotificationManager,
    private val sahhaConfigRepo: SahhaConfigRepo,
    private val sensorRepo: SensorRepo,
    private val healthConnectRepo: HealthConnectRepo,
    private val sahhaErrorLogger: SahhaErrorLogger,
) {
    internal suspend fun configure(
        application: Application,
        sahhaSettings: SahhaSettings,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        try {
            saveConfiguration(sahhaSettings)
            cacheConfiguration(sahhaSettings)
            auth.migrateDataIfNeeded { error, success ->
                if (!success) {
                    callback?.invoke(error, false)
                    return@migrateDataIfNeeded
                }
                continueConfigurationAsync(application, sahhaSettings, callback)
            }
        } catch (e: Exception) {
            Log.w(tag, e.message, e)
            continueConfigurationAsync(application, sahhaSettings, callback)
        }
    }

    private fun cacheConfiguration(sahhaSettings: SahhaSettings) {
        Session.settings = sahhaSettings
    }

    private fun continueConfigurationAsync(
        application: Application,
        sahhaSettings: SahhaSettings,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        defaultScope.launch {
            listOf(
                async { saveNotificationConfig(sahhaSettings.notificationSettings) },
            ).joinAll()

            awaitProcessAndPutDeviceInfo(application)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requestNotificationPermission(application)

            permission.startHcOrNativeDataCollection(application, callback)
        }
    }

    private fun requestNotificationPermission(context: Context) {
        permission.manager.launchPermissionActivity(
            context,
            SahhaNotificationPermissionActivity::class.java,
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
                sensor.stopAllBackgroundTasks(context)
                listOf(
                    async {
                        sensor.startDataCollection(context) { _, success ->
                            if (success) {
//                                sensorRepo.startBackgroundTaskRestarterWorker(
//                                    Constants.WORKER_REPEAT_1_DAY,
//                                    Constants.BACKGROUND_TASK_RESTARTER_WORKER_TAG
//                                )
                            }
                        }
                    },
                    async { sensor.checkAndStartPostWorkers(context) },
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
                sensor.stopAllBackgroundTasks(context)
                listOf(
                    async {
                        sensorRepo.startBatchedDataPostWorker(
                            Constants.WORKER_REPEAT_INTERVAL_MINUTES,
                            Constants.SAHHA_DATA_LOG_WORKER_TAG
                        )
                    },
                    async {
                        sensor.startDataCollection(context) { _, success ->
                            if (success) {
//                                sensorRepo.startHealthConnectQueryWorker(
//                                    Constants.WORKER_REPEAT_INTERVAL_MINUTES,
//                                    Constants.HEALTH_CONNECT_QUERY_WORKER_TAG
//                                )
//                                sensorRepo.startBackgroundTaskRestarterWorker(
//                                    Constants.WORKER_REPEAT_1_DAY,
//                                    Constants.BACKGROUND_TASK_RESTARTER_WORKER_TAG
//                                )
                            }
                        }
                    },
                    async { sensor.checkAndStartPostWorkers(context) },
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