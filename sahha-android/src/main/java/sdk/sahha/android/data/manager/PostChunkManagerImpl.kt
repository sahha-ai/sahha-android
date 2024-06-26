package sdk.sahha.android.data.manager

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.domain.manager.PostChunkManager
import javax.inject.Inject
import javax.inject.Named

private const val tag = "PostChunkManagerImpl"

internal class PostChunkManagerImpl @Inject constructor(
    @Named("BatchMutex") private val mutex: Mutex
) : PostChunkManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var postJobs = emptyList<Job>()
    override var postedChunkCount = 0
    override suspend fun <T> postAllChunks(
        allData: List<T>,
        limit: Int,
        postData: (suspend (chunkedData: List<T>) -> Boolean),
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        mutex.withLock {
            resetCount()
            val chunkedData = allData.chunked(limit)
            val results = mutableListOf<Boolean>()
            for (chunk in chunkedData) {
                postJobs += try {
                    scope.launch {
                        val successful = postData(chunk)
                        results.add(successful)
                        ++postedChunkCount
                    }
                } catch (e: Exception) {
                    Log.d(tag, "Failed to post batch data: ${e.message}")
                    Job()
                }
            }

            postJobs.joinAll()

            val hadFailures = results.contains(false)
            if (hadFailures) {
                callback?.invoke(SahhaErrors.failedToPostAllData, false)
                return
            }

            callback?.invoke(null, true)
        }
    }

    private fun resetCount() {
        postedChunkCount = 0
        postJobs = emptyList()
    }
}