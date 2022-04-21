package sdk.sahha.android.domain.model.categories

import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.domain.use_case.post.PostDeviceDataUseCase
import javax.inject.Inject
import javax.inject.Named

@Keep
class Device @Inject constructor(
    @Named("iosScope") private val ioScope: CoroutineScope,
    private val configDao: ConfigurationDao,
    private val postDeviceDataUseCase: PostDeviceDataUseCase
) {
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