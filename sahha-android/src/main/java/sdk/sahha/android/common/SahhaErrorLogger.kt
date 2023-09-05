package sdk.sahha.android.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import sdk.sahha.android.data.Constants.API_ERROR
import sdk.sahha.android.data.Constants.APPLICATION_ERROR
import sdk.sahha.android.data.Constants.PLATFORM_NAME
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.domain.model.dto.DemographicDto
import sdk.sahha.android.domain.model.error_log.SahhaErrorLog
import sdk.sahha.android.domain.model.error_log.SahhaResponseError
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.source.SahhaConverterUtility

private const val tag = "SahhaErrorLogger"

class SahhaErrorLogger(
    private val context: Context,
    private val sahhaErrorApi: SahhaErrorApi,
    private val mainScope: CoroutineScope,
    private val authRepo: AuthRepo,
    private val sahhaConfigRepo: SahhaConfigRepo
) {
    private var sahhaErrorLog = getNewSahhaErrorLog()
    private suspend fun postErrorLog(
        sahhaErrorLog: SahhaErrorLog,
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    ) {
        val token = authRepo.getToken() ?: ""

        try {
            val response = sahhaErrorApi.postErrorLog(token, sahhaErrorLog)

            if (response.isSuccessful) callback?.invoke(null, true)
            else {
                callback?.invoke("${response.code()}: ${response.message()}", false)
                Log.w(tag, "An error log post ended with a failure response:")
                Log.w(tag, "Response code: ${response.code()}")
                Log.w(tag, "Response message: ${response.message()}")
            }
        } catch (e: Exception) {
            callback?.invoke(e.message, false)
            Log.w(tag, "Failed to post an error log:")
            Log.w(tag, "Exception message: ${e.message}")
        }
    }

    fun api(
        call: Call<ResponseBody>,
        type: String,
        code: Int?,
        message: String
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(call, type, code, message)
            postErrorLog(sahhaErrorLog)
        }
    }

    @JvmName("apiUnit")
    fun api(
        call: Call<Unit>,
        type: String,
        code: Int?,
        message: String
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(call, type, code, message)
            postErrorLog(sahhaErrorLog)
        }
    }

    @JvmName("apiDemographicDto")
    fun api(
        call: Call<DemographicDto>?,
        type: String,
        code: Int?,
        message: String
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(call, type, code, message)
            postErrorLog(sahhaErrorLog)
        }
    }

    fun api(
        call: Call<*>?,
        response: Response<*>?
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(call, response)
            postErrorLog(sahhaErrorLog)
        }
    }

    fun api(
        response: Response<*>,
        type: String
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(response, type)
            postErrorLog(sahhaErrorLog)
        }
    }

    fun application(
        error: String?,
        appMethod: String,
        appBody: String?,
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApplicationLogProperties(error, appMethod, appBody)
            postErrorLog(sahhaErrorLog, callback)
        }
    }

    private fun setApplicationLogProperties(error: String?, appMethod: String, appBody: String?) {
        sahhaErrorLog.errorSource = APPLICATION_ERROR
        error?.also { sahhaErrorLog.errorMessage = it }
        sahhaErrorLog.codeMethod = appMethod
        appBody?.also { sahhaErrorLog.codeBody = it }
    }

    private fun setApiLogProperties(
        call: Call<ResponseBody>?,
        type: String,
        code: Int?,
        message: String
    ) {
        sahhaErrorLog.errorSource = API_ERROR
        call?.also {
            sahhaErrorLog.codeBody =
                SahhaConverterUtility.requestBodyToString(it.request().body) ?: SahhaErrors.noData
            sahhaErrorLog.apiMethod = it.request().method
            sahhaErrorLog.apiURL = it.request().url.encodedPath
        }
        sahhaErrorLog.errorType = type
        code?.also { sahhaErrorLog.errorCode = it }
        sahhaErrorLog.errorMessage = message
    }

    private fun setApiLogProperties(
        response: Response<*>,
        type: String
    ) {
        sahhaErrorLog.errorSource = API_ERROR
        response.raw().request.also { req ->
            sahhaErrorLog.codeBody =
                SahhaConverterUtility.requestBodyToString(req.body) ?: SahhaErrors.noData
            sahhaErrorLog.apiMethod = req.method
            sahhaErrorLog.apiURL = req.url.encodedPath
        }
        sahhaErrorLog.errorType = type
        sahhaErrorLog.errorCode = response.code()
        sahhaErrorLog.errorMessage = response.message()
    }


    @JvmName("setApiLogPropertiesUnit")
    private fun setApiLogProperties(
        call: Call<Unit>?,
        type: String,
        code: Int?,
        message: String
    ) {
        sahhaErrorLog.errorSource = API_ERROR
        call?.also {
            sahhaErrorLog.codeBody =
                SahhaConverterUtility.requestBodyToString(it.request().body) ?: SahhaErrors.noData
            sahhaErrorLog.apiMethod = it.request().method
            sahhaErrorLog.apiURL = it.request().url.encodedPath
        }
        sahhaErrorLog.errorType = type
        code?.also { sahhaErrorLog.errorCode = it }
        sahhaErrorLog.errorMessage = message
    }

    @JvmName("setApiLogPropertiesDemographicDto")
    private fun setApiLogProperties(
        call: Call<DemographicDto>?,
        type: String,
        code: Int?,
        message: String
    ) {
        sahhaErrorLog.errorSource = API_ERROR
        call?.also {
            sahhaErrorLog.codeBody =
                SahhaConverterUtility.requestBodyToString(it.request().body) ?: SahhaErrors.noData
            sahhaErrorLog.apiMethod = it.request().method
            sahhaErrorLog.apiURL = it.request().url.encodedPath
        }
        sahhaErrorLog.errorType = type
        code?.also { sahhaErrorLog.errorCode = it }
        sahhaErrorLog.errorMessage = message
    }

    private fun setApiLogProperties(
        call: Call<*>?,
        response: Response<*>?
    ) {
        var sahhaResponseError: SahhaResponseError? = null
        sahhaErrorLog.errorSource = API_ERROR

        response?.also { r ->
            sahhaResponseError =
                SahhaConverterUtility.responseBodyToSahhaResponseError(r.errorBody())
            sahhaErrorLog.codeBody =
                r.errorBody()?.charStream()?.readText() ?: SahhaErrors.noData
        }

        call?.also { c ->
            sahhaErrorLog.apiMethod = c.request().method
            sahhaErrorLog.apiURL = c.request().url.encodedPath
        }

        sahhaResponseError?.also {
            sahhaErrorLog.errorCode = it.statusCode
            sahhaErrorLog.errorType = it.location
            sahhaErrorLog.errorMessage = it.title
        }
    }

    @SuppressLint("HardwareIds")
    private suspend fun setStaticParameters() {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appId = packageInfo.packageName
        val versionName: String? = packageInfo.versionName
        val config = sahhaConfigRepo.getConfig()

        sahhaErrorLog.sdkId = config.framework
        sahhaErrorLog.sdkVersion = sdk.sahha.android.BuildConfig.SDK_VERSION_NAME
        sahhaErrorLog.appId = appId
        sahhaErrorLog.appVersion = versionName ?: "Unknown"
        sahhaErrorLog.deviceType = Build.MANUFACTURER
        sahhaErrorLog.deviceModel = Build.MODEL
        sahhaErrorLog.system = PLATFORM_NAME
        sahhaErrorLog.systemVersion =
            "Android SDK: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})"
    }

    private fun getNewSahhaErrorLog(): SahhaErrorLog {
        return SahhaErrorLog(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
        )
    }
}