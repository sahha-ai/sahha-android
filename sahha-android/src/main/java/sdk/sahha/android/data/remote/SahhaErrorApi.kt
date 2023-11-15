package sdk.sahha.android.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.error_log.SahhaErrorLog

interface SahhaErrorApi {
    @POST("error")
    suspend fun postErrorLog(
        @Header(Constants.AUTHORIZATION_HEADER) profileToken: String?,
        @Body sahhaErrorLog: SahhaErrorLog
    ): Response<ResponseBody>
}