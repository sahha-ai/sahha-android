package sdk.sahha.android.interaction

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.di.DefaultScope
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
    @DefaultScope private val defaultScope: CoroutineScope,
    internal val auth: AuthInteractionManager,
    internal val permission: PermissionInteractionManager,
    internal val userData: UserDataInteractionManager,
    internal val sensor: SensorInteractionManager,
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
                Sahha.config = sahhaConfigRepo.getConfig()

                listOf(
                    async { saveNotificationConfig(sahhaSettings.notificationSettings) },
                ).joinAll()

                // TODO TO FIX: Crashes when HC not available
//                userData.processAndPutDeviceInfo(application) { _, _ ->
//                    permission.checkPermissionsAndStart(
//                        application, callback
//                    )
//                }

                awaitProcessAndPutDeviceInfo(application)
                permission.checkHcAvailabilityAndStart(application)
            }
        }
    }

    private suspend fun awaitProcessAndPutDeviceInfo(context: Context) =
        suspendCancellableCoroutine { cont ->
            println("awaitProcessAndPutDeviceInfo0001")
            defaultScope.launch {
                println("awaitProcessAndPutDeviceInfo0002")
                userData.processAndPutDeviceInfo(context) { _, success ->
                    println("awaitProcessAndPutDeviceInfo0003: $success")
                    if (cont.isActive) cont.resume(success)
                }
            }
        }

    internal fun startNative(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        try {
            defaultScope.launch {
                sensorRepo.stopAllWorkers()
                Sahha.config = sahhaConfigRepo.getConfig()
                listOf(
                    async { sensor.startDataCollection(callback) },
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

    internal fun startHealthConnect(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        println("startHealthConnect0000")
        try {
            println("startHealthConnect0001")
            defaultScope.launch {
                println("startHealthConnect0002")
                sensorRepo.stopAllWorkers()
                Sahha.config = sahhaConfigRepo.getConfig()
                sensor.startHealthConnectPostWorker(callback)
            }
        } catch (e: Exception) {
            println("startHealthConnect0003")
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