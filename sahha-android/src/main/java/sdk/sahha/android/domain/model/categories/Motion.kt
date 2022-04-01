package sdk.sahha.android.domain.model.categories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

    fun postData(
        sensor: Enum<SahhaSensor>,
        callback: ((error: String?, success: String?) -> Unit)
    ) {
        ioScope.launch {
            val config = configDao.getConfig()
            if (!config.sensorArray.contains(sensor.ordinal)) {
                callback(SahhaErrors.sensorNotEnabled(sensor), null)
                return@launch
            }

            if (sensor.ordinal == SahhaSensor.SLEEP.ordinal) {
                postSleepDataUseCase(callback)
            }
        }
    }
}