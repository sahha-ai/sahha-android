package sdk.sahha.android.domain.use_case.post

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.repository.SensorRepo
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PostSleepDataUseCase @Inject constructor  (
    private val repository: SensorRepo,
    private val dao: SleepDao,
    internal val chunkManager: PostChunkManager,
    @IoScope private val ioScope: CoroutineScope
) {
    suspend operator fun invoke(sleepData: List<SleepDto>, callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        chunkManager.postAllChunks(
            sleepData,
            Constants.DEFAULT_POST_LIMIT,
            { chunk ->
                suspendCoroutine { cont ->
                    ioScope.launch {
                        repository.postSleepData(chunk) { _, successful ->
                            dao.clearSleepDto(chunk)
                            cont.resume(successful)
                        }
                    }
                }
            }
        ) { error, successful -> callback?.invoke(error, successful) }
    }
}