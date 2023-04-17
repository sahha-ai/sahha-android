package sdk.sahha.android.interaction

import android.app.Application
import kotlinx.coroutines.*
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.di.MainScope
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.background.StartCollectingPhoneScreenLockDataUseCase
import sdk.sahha.android.domain.use_case.background.StartCollectingStepCounterData
import sdk.sahha.android.domain.use_case.post.*
import sdk.sahha.android.source.*
import java.util.*
import javax.inject.Inject

private const val tag = "SahhaInteractionManager"

class SahhaInteractionManager @Inject constructor(
    @MainScope private val mainScope: CoroutineScope,
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

            mainScope.launch {
                Sahha.config = configurationDao.getConfig()

                listOf(
                    async { saveNotificationConfig(sahhaSettings.notificationSettings) },
                    async { userData.processAndPutDeviceInfo(application) }
                ).joinAll()

                start(callback)
            }
        }
    }

    internal fun start(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        try {
            runBlocking {
                sensorRepo.stopAllWorkers()
                Sahha.config = configurationDao.getConfig()
                listOf(
                    async { sensor.startDataCollection(callback) },
                    async { sensor.checkAndStartPostWorkers() },
                ).joinAll()
                callback?.invoke(null, true)
            }
        } catch (e: Exception) {
            callback?.invoke("Error: ${e.message}", false)
            sahhaErrorLogger.application(e.message, "start", null)
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