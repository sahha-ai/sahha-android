package sdk.sahha.android.domain.use_case.post

import android.content.Context
import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaResponseHandler
import sdk.sahha.android.common.Session
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.manager.ConnectionStateManager
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.BatchedDataRepo
import sdk.sahha.android.domain.transformer.AggregateDataLogTransformer
import sdk.sahha.android.domain.use_case.CalculateBatchLimit
import sdk.sahha.android.domain.use_case.background.FilterActivityOverlaps
import sdk.sahha.android.domain.use_case.metadata.AddMetadata
import sdk.sahha.android.source.Sahha
import javax.inject.Inject

private const val TAG = "PostBatchData"

internal class PostBatchData @Inject constructor(
    private val context: Context,
    private val api: SahhaApi,
    private val chunkManager: PostChunkManager,
    private val authRepo: AuthRepo,
    private val batchRepo: BatchedDataRepo,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val calculateBatchLimit: CalculateBatchLimit,
    private val filterActivityOverlaps: FilterActivityOverlaps,
    private val addMetadata: AddMetadata,
    private val dataLogTransformer: AggregateDataLogTransformer
) {
    suspend operator fun invoke(
        batchedData: List<SahhaDataLog>,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)? = null
    ) {
        if (batchedData.isEmpty()) {
            callback?.invoke("No data found", true)
            return
        }

        val filtered = filterActivityOverlaps(batchedData)
        val metadataAdded = addMetadata(
            dataList = filtered,
            saveData = batchRepo::saveBatchedData
        )
        chunkManager.postAllChunks(
            allData = metadataAdded,
            limit = calculateBatchLimit(),
            postData = { chunk ->
                val token = authRepo.getToken() ?: ""
                val chunkDto = chunk.map { log -> dataLogTransformer.transformDataLog(log) }

                try {
                    val response = api.postSahhaDataLogDto(
                        TokenBearer(token), chunkDto
                    )

                    handleResponse(
                        context = context,
                        response = response,
                        retryLogic = { api.postSahhaDataLogDto(TokenBearer(token), chunkDto) },
                        successfulLogic = {
                            batchRepo.deleteBatchedData(chunk)
                        },
                        callback = null
                    )
                    ResponseCode.isSuccessful(response.code())
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                    false
                }
            },
            callback = callback
        )
    }

    private suspend fun handleResponse(
        context: Context,
        response: Response<ResponseBody>,
        retryLogic: suspend (() -> Response<ResponseBody>),
        successfulLogic: (suspend () -> Unit)? = null,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            val code = response.code()

            if (ResponseCode.accountRemoved(code)) {
                val error = "Account does not exist, stopping all tasks"
                Log.w(TAG, error)
                Sahha.sim.auth.deauthenticate { error, _ -> error?.also { Log.w(TAG, it) } }
                Sahha.sim.sensor.stopAllBackgroundTasks(context)
                Sahha.sim.sensor.killMainService(context)
                callback?.invoke(error, false)
                return
            }

            if (ResponseCode.isUnauthorized(code)) {
                if (Session.tokenRefreshAttempted) {
                    callback?.invoke("Token refresh already attempted", false)
                    return
                }

                SahhaResponseHandler.checkTokenExpired(code) {
                    val retryResponse = retryLogic()
                    handleResponse(
                        context = context,
                        response = retryResponse,
                        retryLogic = { retryLogic() },
                        callback = callback,
                        successfulLogic = successfulLogic
                    )
                    callback?.invoke(SahhaErrors.attemptingTokenRefresh, false)
                    Session.tokenRefreshAttempted = true
                }
                return
            }

            if (ResponseCode.isSuccessful(code)) {
                successfulLogic?.invoke()
                callback?.invoke(null, true)
                Log.d(TAG, "${code}: ${response.message()}")
                return
            }

            callback?.invoke(
                "${code}: ${response.message()}",
                false
            )

            sahhaErrorLogger.apiFromJsonArray(response)
        } catch (e: Exception) {
            callback?.invoke(e.message, false)

            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                TAG,
                "handleResponse",
                e.stackTraceToString(),
            )
        }
    }
}