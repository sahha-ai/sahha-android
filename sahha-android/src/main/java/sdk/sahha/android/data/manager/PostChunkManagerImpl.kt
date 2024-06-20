package sdk.sahha.android.data.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.domain.manager.PostChunkManager
import javax.inject.Inject

private const val tag = "PostChunkManagerImpl"

internal class PostChunkManagerImpl: PostChunkManager {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var postJobs = emptyList<Job>()
    override var postedChunkCount = 0
    override suspend fun <T> postAllChunks(
        allData: List<T>,
        limit: Int,
        postData: (suspend (chunkedData: List<T>) -> Boolean),
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        resetCount()
        val chunkedData = allData.chunked(limit)
        var hasFailed = false
        for (chunk in chunkedData) {
            postJobs += scope.launch {
                val success = postData(chunk)
                if (!success) {
                    hasFailed = true
                    scope.cancel()
                }

                ++postedChunkCount
            }
        }

        postJobs.joinAll()

        if (hasFailed) {
            callback?.invoke(SahhaErrors.failedToPostAllData, false)
            return
        }

        callback?.invoke(null, true)
    }

    private fun resetCount() {
        postedChunkCount = 0
        postJobs = emptyList()
    }
}