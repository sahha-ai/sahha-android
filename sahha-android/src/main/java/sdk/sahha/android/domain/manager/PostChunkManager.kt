package sdk.sahha.android.domain.manager

import okhttp3.ResponseBody
import retrofit2.Response

interface PostChunkManager {
    suspend fun <T> sendDataInChunks(
        data: List<T>,
        sendData: suspend (List<T>) -> Boolean
    ): Boolean
}