package sdk.sahha.android.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaResponseHandler
import sdk.sahha.android.common.SahhaResponseHandler.returnFormattedResponse
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.model.analyze.AnalyzeRequest
import sdk.sahha.android.domain.model.dto.DemographicDto
import sdk.sahha.android.domain.model.dto.toSahhaDemographic
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.source.SahhaDemographic

class UserDataRepoImpl(
    private val ioScope: CoroutineScope,
    private val authRepo: AuthRepo,
    private val api: SahhaApi,
    private val sahhaErrorLogger: SahhaErrorLogger,
) : UserDataRepo {
    override suspend fun getAnalysis(
        dates: Pair<String, String>?,
        callback: ((error: String?, successful: String?) -> Unit)?,
    ) {
        try {
            val response = getDetectedAnalysisCall(dates)

            if (ResponseCode.isUnauthorized(response.code())) {
                callback?.also { it(SahhaErrors.attemptingTokenRefresh, null) }
                SahhaResponseHandler.checkTokenExpired(response.code()) {
                    getAnalysis(dates, callback)
                }
                sahhaErrorLogger.application(
                    SahhaErrors.attemptingTokenRefresh,
                    "getAnalysis",
                    null
                )
                return
            }

            if (ResponseCode.isSuccessful(response.code())) {
                returnFormattedResponse(response, callback)
                return
            }

            callback?.invoke(
                "${response.code()}: ${response.message()}",
                null
            )
            sahhaErrorLogger.api(response, SahhaErrors.typeRequest)
        } catch (e: Exception) {
            sahhaErrorLogger.application(
                e.message,
                "getAnalysis",
                dates?.toString()
            )
            callback?.invoke(e.message, null)
        }
    }

    override suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        try {
            val call = getDemographicCall()
            call.enqueue(
                object : Callback<DemographicDto> {
                    override fun onResponse(
                        call: Call<DemographicDto>,
                        response: Response<DemographicDto>
                    ) {
                        ioScope.launch {
                            if (ResponseCode.isUnauthorized(response.code())) {
                                callback?.also { it(SahhaErrors.attemptingTokenRefresh, null) }
                                SahhaResponseHandler.checkTokenExpired(response.code()) {
                                    getDemographic(callback)
                                }
                                sahhaErrorLogger.application(
                                    SahhaErrors.attemptingTokenRefresh,
                                    "getDemographic",
                                    null
                                )
                                return@launch
                            }

                            if (ResponseCode.isSuccessful(response.code())) {
                                val sahhaDemographic = response.body()?.toSahhaDemographic()

                                when (sahhaDemographic) {
                                    null -> callback?.invoke(SahhaErrors.noDemographics, null)
                                    else -> callback?.invoke(null, sahhaDemographic)
                                }

                                return@launch
                            }

                            callback?.also {
                                it(
                                    "${response.code()}: ${response.message()}",
                                    null
                                )
                            }

                            sahhaErrorLogger.api(call, response)
                        }
                    }

                    override fun onFailure(call: Call<DemographicDto>, t: Throwable) {
                        callback?.also { it(t.message, null) }
                        sahhaErrorLogger.api(
                            call,
                            SahhaErrors.typeResponse,
                            null,
                            t.message ?: SahhaErrors.responseFailure
                        )
                    }
                }
            )
        } catch (e: Exception) {
            callback?.also { it(e.message, null) }

            sahhaErrorLogger.application(
                e.message,
                "getDemographic",
                null
            )
        }
    }

    override suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            val call = postDemographicResponse(sahhaDemographic)
            call.enqueue(
                object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        ioScope.launch {
                            if (ResponseCode.isUnauthorized(response.code())) {
                                callback?.also { it(SahhaErrors.attemptingTokenRefresh, false) }
                                SahhaResponseHandler.checkTokenExpired(response.code()) {
                                    postDemographic(sahhaDemographic, callback)
                                }
                                sahhaErrorLogger.application(
                                    SahhaErrors.attemptingTokenRefresh,
                                    "postDemographic",
                                    null
                                )
                                return@launch
                            }

                            if (ResponseCode.isSuccessful(response.code())) {
                                callback?.also { it(null, true) }
                                return@launch
                            }

                            callback?.also {
                                it(
                                    "${response.code()}: ${response.message()}",
                                    false
                                )
                            }

                            sahhaErrorLogger.api(call, response)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        callback?.also { it(t.message, false) }
                        sahhaErrorLogger.api(
                            call,
                            SahhaErrors.typeResponse,
                            null,
                            t.message ?: SahhaErrors.responseFailure
                        )
                    }
                }
            )
        } catch (e: Exception) {
            callback?.also { it(e.message, false) }

            sahhaErrorLogger.application(
                e.message,
                "postDemographic",
                sahhaDemographic.toString()
            )
        }
    }

    private suspend fun getDemographicCall(): Call<DemographicDto> {
        val token = authRepo.getToken() ?: ""
        return api.getDemographic(TokenBearer(token))
    }

    private suspend fun postDemographicResponse(sahhaDemographic: SahhaDemographic): Call<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.putDemographic(TokenBearer(token), sahhaDemographic)
    }

    private suspend fun getDetectedAnalysisCall(
        datesISO: Pair<String, String>?,
    ): Response<ResponseBody> {
        return datesISO?.let { it ->
            getAnalysisResponse(it.first, it.second)
        } ?: getAnalysisResponse()
    }

    private suspend fun getAnalysisResponse(): Response<ResponseBody> {
        val analyzeRequest = AnalyzeRequest(
            null,
            null,
        )
        val token = authRepo.getToken() ?: ""

        return api.analyzeProfileResponse(TokenBearer(token), analyzeRequest)
    }

    private suspend fun getAnalysisResponse(
        startDate: String,
        endDate: String,
    ): Response<ResponseBody> {
        val analyzeRequest = AnalyzeRequest(
            startDate,
            endDate,
        )
        val token = authRepo.getToken() ?: ""

        return api.analyzeProfileResponse(TokenBearer(token), analyzeRequest)
    }
}