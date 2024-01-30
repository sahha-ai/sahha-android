package sdk.sahha.android.domain.manager

internal interface PostChunkManager {
    var postedChunkCount: Int
    suspend fun <T> postAllChunks(
        allData: List<T>,
        limit: Int,
        postData: (suspend (chunkedData: List<T>) -> Boolean),
        callback: (suspend (error: String?, successful: Boolean) -> Unit)? = null
    )
}