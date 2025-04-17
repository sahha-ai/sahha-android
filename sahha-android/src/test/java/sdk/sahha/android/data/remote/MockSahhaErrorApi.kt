package sdk.sahha.android.data.remote

import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.model.error_log.SahhaErrorLog

internal class MockSahhaErrorApi(
    private val delegate: BehaviorDelegate<SahhaErrorApi>
): SahhaErrorApi {
    override suspend fun postErrorLog(
        profileToken: String?,
        sahhaErrorLog: SahhaErrorLog
    ): Response<ResponseBody> {
        val responseBody = """{"key":"value"}""".toResponseBody()

        val response = profileToken?.let {
                if (profileToken.isNotEmpty())
                    Response.success(responseBody)
                else
                    Response.error(500, responseBody)
        }

        return delegate.returningResponse(response).postErrorLog(profileToken, sahhaErrorLog)
    }

}