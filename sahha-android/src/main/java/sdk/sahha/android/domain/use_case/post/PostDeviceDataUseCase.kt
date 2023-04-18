package sdk.sahha.android.domain.use_case.post

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.local.dao.DeviceUsageDao
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PostDeviceDataUseCase @Inject constructor(
    private val repository: SensorRepo,
    private val dao: DeviceUsageDao,
    internal val chunkManager: PostChunkManager,
    @IoScope private val ioScope: CoroutineScope
) {
    suspend operator fun invoke(
        deviceLockData: List<PhoneUsage>,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        chunkManager.postAllChunks(
            deviceLockData,
            Constants.DEVICE_LOCK_POST_LIMIT,
            { chunk ->
                suspendCoroutine<Boolean> { cont ->
                    ioScope.launch {
                        repository.postPhoneScreenLockData(chunk) { _, successful ->
                            dao.clearUsages(chunk)
                            cont.resume(successful)
                        }
                    }
                }
            }
        ) { error, successful -> callback?.invoke(error, successful) }
    }
}