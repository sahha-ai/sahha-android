package sdk.sahha.android.data.manager

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.domain.manager.PostChunkManager
import kotlin.math.min

private const val CHUNK_SIZE = 8192
private const val tag = "PostChunkManagerImpl"

class PostChunkManagerImpl : PostChunkManager {
    override suspend fun <T> sendDataInChunks(
        data: List<T>,
        sendData: suspend (List<T>) -> Boolean
    ): Boolean {
        val totalSize = data.size
        var bytesSent = 0

        Log.d(tag, "sendDataInChunks: totalSize: $totalSize")
        while (bytesSent < totalSize) {
            val chunkSize = min(CHUNK_SIZE, totalSize - bytesSent)
            val chunk = data.subList(bytesSent, bytesSent + chunkSize)
            Log.d(tag, "sendDataInChunks: chunkSize: $chunkSize")

            val result = try {
                sendData(chunk)
            } catch (e: Exception) {
                Log.e(tag, e.message, e)
                false
            }
            Log.d(tag, "sendDataInChunks: result: $result")

            if (!result) {
                return false
            }

            bytesSent += chunkSize
            Log.d(tag, "sendDataInChunks: bytesSent: $bytesSent")
        }

        return true
    }
}