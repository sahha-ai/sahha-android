package sdk.sahha.android.domain.manager

interface PostChunkManager {
    var postedChunkCount: Int
    suspend fun <T> postAllChunks(
        allData: List<T>,
        limit: Int,
        postData: (suspend (chunkedData: List<T>) -> Boolean),
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    )
}