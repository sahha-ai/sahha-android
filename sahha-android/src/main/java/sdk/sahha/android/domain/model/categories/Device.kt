package sdk.sahha.android.domain.model.categories

import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.domain.model.enums.SahhaSensor
import sdk.sahha.android.domain.use_case.post.PostDeviceDataUseCase
import javax.inject.Inject
import javax.inject.Named

@Keep
class Device @Inject constructor(
    @Named("iosScope") private val ioScope: CoroutineScope,
    private val configDao: ConfigurationDao,
    private val postDeviceDataUseCase: PostDeviceDataUseCase
) {
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

            if (sensor.ordinal == SahhaSensor.DEVICE.ordinal) {
                postDeviceDataUseCase(callback)
            }
        }
    }

    //TODO: For demo only
    fun getData(callback: ((data: List<String>) -> Unit)) {
        Sahha.di.ioScope.launch {
            val lockData = Sahha.di.deviceUsageDao.getUsages()
            val lockDataString = mutableListOf<String>()
            lockData.mapTo(lockDataString) {
                when {
                    it.isLocked -> {
                        "Locked at ${it.createdAt}"
                    }
                    else -> {
                        "Unlocked at ${it.createdAt}"
                    }
                }
            }
            callback(lockDataString)
        }
    }
}