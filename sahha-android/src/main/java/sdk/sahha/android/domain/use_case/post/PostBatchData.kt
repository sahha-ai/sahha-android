package sdk.sahha.android.domain.use_case.post

import android.content.Context
import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaResponseHandler
import sdk.sahha.android.common.Session
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.manager.PostChunkManager
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.BatchedDataRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility
import javax.inject.Inject

private const val tag = "PostBatchData"

internal class PostBatchData @Inject constructor(
    private val context: Context,
    private val api: SahhaApi,
    private val chunkManager: PostChunkManager,
    private val authRepo: AuthRepo,
    private val batchRepo: BatchedDataRepo,
    private val sahhaErrorLogger: SahhaErrorLogger
) {
    suspend operator fun invoke(
        batchedData: List<SahhaDataLog>,
        chunkBytes: Int = Constants.DATA_LOG_LIMIT_BYTES,
        callback: (suspend (error: String?, successful: Boolean) -> Unit)? = null
    ) {
        if (Session.batchedDataPosting) {
            callback?.invoke("Batched data posting already in progress", false)
            return
        }

        if (batchedData.isEmpty()) {
            callback?.invoke("No data found", true)
            return
        }

        Session.batchedDataPosting = true
        val sample = batchedData.random()
        val approximateBytesPerLog =
            SahhaConverterUtility.convertToJsonString(sample).toByteArray().size

        chunkManager.postAllChunks(
            allData = batchedData,
            limit = chunkBytes / approximateBytesPerLog,
            postData = { chunk ->
                Session.batchPostInProgress = true
                val token = authRepo.getToken() ?: ""

                try {
                    val response = api.postSahhaDataLogs(TokenBearer(token), chunk)
                    handleResponse(
                        context = context,
                        response = response,
                        retryLogic = { api.postSahhaDataLogs(TokenBearer(token), chunk) },
                        successfulLogic = {
                            batchRepo.deleteBatchedData(chunk)
                            Session.batchPostInProgress = false
                        },
                        callback = null
                    )
                    ResponseCode.isSuccessful(response.code())
                } catch (e: Exception) {
                    Log.e(tag, e.message, e)
                    Session.batchPostInProgress = false
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
                Log.w(tag, error)
                Sahha.sim.auth.deauthenticate { error, _ -> error?.also { Log.w(tag, it) } }
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
                Session.batchedDataPosting = false
                return
            }

            Session.batchedDataPosting = false
            callback?.invoke(
                "${code}: ${response.message()}",
                false
            )

            sahhaErrorLogger.apiFromJsonArray(response)
        } catch (e: Exception) {
            Session.batchedDataPosting = false
            callback?.invoke(e.message, false)

            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                tag,
                "handleResponse",
                e.stackTraceToString(),
            )
        }
    }
}