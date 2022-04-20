package sdk.sahha.android.domain.model.categories

import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.domain.model.enums.SahhaActivityStatus
import sdk.sahha.android.domain.model.enums.SahhaSensor
import sdk.sahha.android.domain.use_case.permissions.ActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.PromptUserToActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import sdk.sahha.android.domain.use_case.post.PostSleepDataUseCase
import javax.inject.Inject
import javax.inject.Named

@Keep
class Motion @Inject constructor(
    setPermissionLogicUseCase: SetPermissionLogicUseCase,
    private val configDao: ConfigurationDao,
    @Named("ioScope") private val ioScope: CoroutineScope,
    private val activateUseCase: ActivateUseCase,
    private val promptUserToActivateUseCase: PromptUserToActivateUseCase,
    private val postSleepDataUseCase: PostSleepDataUseCase
) : RequiresPermission(
    setPermissionLogicUseCase
) {
    fun activate(_activityCallback: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)) {
        activateUseCase(_activityCallback)
    }

    fun promptUserToActivate(_activityCallback: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)) {
        promptUserToActivateUseCase(_activityCallback)
    }

    fun postSensorData(
        sensor: Enum<SahhaSensor>,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        ioScope.launch {
            val config = configDao.getConfig()
            if (!config.sensorArray.contains(sensor.ordinal)) {
                callback(SahhaErrors.sensorNotEnabled(sensor), false)
                return@launch
            }

            if (sensor.ordinal == SahhaSensor.sleep.ordinal) {
                postSleepDataUseCase(callback)
            }
        }
    }

    //TODO: For demo only
    fun getData(callback: ((data: List<String>) -> Unit)) {
        Sahha.di.ioScope.launch {
            val sleepData = Sahha.di.sleepDao.getSleepDto()
            val sleepDataString = mutableListOf<String>()
            sleepData.mapTo(sleepDataString) {
                "Minutes slept: ${it.minutesSlept}" +
                        "\nStarted: ${it.startDateTime}" +
                        "\nEnded: ${it.endDateTime}" +
                        "\nCreated at: ${it.createdAt}"
            }
            callback(sleepDataString)
        }
    }
}