package sdk.sahha.android.data.manager

import sdk.sahha.android.domain.manager.PostChunkManager

private const val tag = "PostChunkManagerImpl"

class PostChunkManagerImpl : PostChunkManager {
    override var postedChunkCount = 0
    override suspend fun <T> postAllChunks(
        allData: List<T>,
        limit: Int,
        postData: (suspend (chunkedData: List<T>) -> Boolean)
    ) {
        resetCount()
        val chunkedData = allData.chunked(limit)
        for (chunk in chunkedData) {
            val success = postData(chunk)
            if (!success) break

            ++postedChunkCount
        }
    }

    private fun resetCount() {
        postedChunkCount = 0
    }
}