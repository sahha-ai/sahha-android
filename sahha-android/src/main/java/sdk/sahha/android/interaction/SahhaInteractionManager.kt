package sdk.sahha.android.interaction

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaNotificationConfiguration
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings
import javax.inject.Inject

private const val tag = "SahhaInteractionManager"

internal class SahhaInteractionManager @Inject constructor(
    @DefaultScope private val defaultScope: CoroutineScope,
    internal val auth: AuthInteractionManager,
    internal val permission: PermissionInteractionManager,
    internal val userData: UserDataInteractionManager,
    internal val sensor: SensorInteractionManager,
    private val configurationDao: ConfigurationDao,
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
                Sahha.config = configurationDao.getConfig()

                listOf(
                    async { saveNotificationConfig(sahhaSettings.notificationSettings) },
                ).joinAll()

                userData.processAndPutDeviceInfo(application) { _, _ ->
                    startNative(callback)
                }
            }
        }
    }

    internal fun startNative(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        try {
            defaultScope.launch {
                sensorRepo.stopAllWorkers()
                Sahha.config = configurationDao.getConfig()
                listOf(
                    async { sensor.startDataCollection(callback) },
                    async { sensor.checkAndStartPostWorkers() },
                ).joinAll()
                callback?.invoke(null, true)
            }
        } catch (e: Exception) {
            sahhaErrorLogger.application(e.message, "startNative", null)
            callback?.invoke("Error: ${e.message}", false)
        }
    }

    internal fun startHealthConnect(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        try {
            defaultScope.launch { 
                sensorRepo.stopAllWorkers()
                Sahha.config = configurationDao.getConfig()
                sensor.startHealthConnectPostWorker()
                callback?.invoke(null, true)
            }
        } catch (e: Exception) {
            sahhaErrorLogger.application(e.message, "startHealthConnect", null)
            callback?.invoke("Error: ${e.message}", false)
        }
    }

    private suspend fun saveConfiguration(
        settings: SahhaSettings
    ) {
        val sensorEnums = settings.sensors?.let {
            convertToEnums(it)
        } ?: convertToEnums(SahhaSensor.values().toSet())

        configurationDao.saveConfig(
            SahhaConfiguration(
                settings.environment.ordinal,
                settings.framework.name,
                sensorEnums,
                false
            )
        )
    }

    private suspend fun saveNotificationConfig(config: SahhaNotificationConfiguration?) {
        configurationDao.saveNotificationConfig(
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