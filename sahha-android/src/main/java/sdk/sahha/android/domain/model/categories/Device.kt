package sdk.sahha.android.domain.model.categories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.domain.model.enums.SahhaSensor
import sdk.sahha.android.domain.use_case.post.PostDeviceDataUseCase
import javax.inject.Inject
import javax.inject.Named

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
}