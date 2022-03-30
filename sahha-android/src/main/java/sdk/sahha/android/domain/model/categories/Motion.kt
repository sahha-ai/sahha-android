package sdk.sahha.android.domain.model.categories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.model.enums.SahhaActivityStatus
import sdk.sahha.android.domain.model.enums.SahhaSensor
import sdk.sahha.android.domain.use_case.permissions.ActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.PromptUserToActivateUseCase
import sdk.sahha.android.domain.use_case.permissions.SetPermissionLogicUseCase
import javax.inject.Inject
import javax.inject.Named

class Motion @Inject constructor(
    setPermissionLogicUseCase: SetPermissionLogicUseCase,
    private val activateUseCase: ActivateUseCase,
    private val promptUserToActivateUseCase: PromptUserToActivateUseCase,
    @Named("ioScope") private val ioScope: CoroutineScope,
    private val configDao: ConfigurationDao,
    private val sleepDao: SleepDao,
    private val movementDao: MovementDao,
    private val decryptor: Decryptor,
    private val api: SahhaApi,
) : RequiresPermission(
    setPermissionLogicUseCase
) {
    fun activate(_activityCallback: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)) {
        activateUseCase(_activityCallback)
    }

    fun promptUserToActivate(_activityCallback: ((sahhaActivityStatus: Enum<SahhaActivityStatus>) -> Unit)) {
        promptUserToActivateUseCase(_activityCallback)
    }

    fun postData(sensor: Enum<SahhaSensor>) {
        ioScope.launch {
            val config = configDao.getConfig()
            if(!config.sensorArray.contains(sensor.ordinal)) return@launch

            if(sensor.ordinal == SahhaSensor.SLEEP.ordinal) {
                val call = callSleepApi()
            }
        }
    }

    private suspend fun callSleepApi(): Call<ResponseBody> {
        return api.sendSleepDataRange(
            decryptor.decryptToken(),
            sleepDao.getSleepDto()
        )
    }
}