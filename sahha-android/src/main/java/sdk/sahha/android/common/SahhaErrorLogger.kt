package sdk.sahha.android.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import okhttp3.internal.closeQuietly
import retrofit2.Call
import retrofit2.Response
import sdk.sahha.android.common.Constants.API_ERROR
import sdk.sahha.android.common.Constants.APPLICATION_ERROR
import sdk.sahha.android.common.Constants.PLATFORM_NAME
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.domain.model.dto.DemographicDto
import sdk.sahha.android.domain.model.error_log.SahhaErrorLog
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.source.SahhaConverterUtility
import sdk.sahha.android.source.SahhaFramework

private const val tag = "SahhaErrorLogger"

internal class SahhaErrorLogger(
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
        val token = authRepo.getToken()

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
        message: String,
        errorBody: ResponseBody?
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(call, type, code, message, errorBody)
            postErrorLog(sahhaErrorLog)
        }
    }

    @JvmName("apiDemographicDto")
    fun api(
        call: Call<DemographicDto>?,
        type: String,
        code: Int?,
        message: String,
        errorBody: ResponseBody?
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(call, type, code, message, errorBody)
            postErrorLog(sahhaErrorLog)
        }
    }

    fun api(
        response: Response<*>,
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(response)
            postErrorLog(sahhaErrorLog)
        }
    }

    fun apiFromJsonArray(
        response: Response<*>,
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogPropertiesJsonArray(response)
            postErrorLog(sahhaErrorLog)
        }
    }

    fun application(
        message: String,
        path: String,
        method: String,
        body: String? = null,
        framework: SahhaFramework = SahhaFramework.android_kotlin,
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    ) {
        mainScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApplicationLogProperties(framework, message, path, method, body)
            postErrorLog(sahhaErrorLog, callback)
        }
    }

    private fun setApplicationLogProperties(
        framework: SahhaFramework,
        message: String,
        path: String,
        method: String,
        body: String? = null,
    ) {
        sahhaErrorLog.errorSource = APPLICATION_ERROR
        sahhaErrorLog.errorMessage = message
        sahhaErrorLog.codePath = path
        sahhaErrorLog.codeMethod = method
        sahhaErrorLog.codeBody = body
        sahhaErrorLog.errorLocation = framework.name
    }

    private fun setApiLogProperties(
        call: Call<ResponseBody>?,
        location: String,
        code: Int?,
        message: String,
        errorBody: ResponseBody?
    ) {
        sahhaErrorLog.errorSource = API_ERROR
        call?.also {
            sahhaErrorLog.codeBody =
                SahhaConverterUtility.requestBodyToString(it.request().body)
                    ?: SahhaErrors.noCodeBody
            sahhaErrorLog.codeMethod = it.request().method
            sahhaErrorLog.codePath = it.request().url.encodedPath
        }
        sahhaErrorLog.errorLocation = location
        code?.also { sahhaErrorLog.errorCode = it }
        sahhaErrorLog.errorMessage = message
        sahhaErrorLog.errorBody = readErrorBody(errorBody)
    }

    private fun setApiLogProperties(
        response: Response<*>,
    ) {
        sahhaErrorLog.errorSource = API_ERROR
        response.raw().request.also { req ->
            sahhaErrorLog.codeBody =
                SahhaConverterUtility.requestBodyToString(req.body) ?: SahhaErrors.noCodeBody
            sahhaErrorLog.codeMethod = req.method
            sahhaErrorLog.codePath = req.url.encodedPath
        }

        sahhaErrorLog.errorLocation = getErrorLocation(response.errorBody())
        sahhaErrorLog.errorCode = response.code()
        sahhaErrorLog.errorMessage = response.message()
        sahhaErrorLog.errorBody = readErrorBody(response.errorBody())
    }

    private fun setApiLogPropertiesJsonArray(
        response: Response<*>,
    ) {
        sahhaErrorLog.errorSource = API_ERROR
        response.raw().request.also { req ->
            sahhaErrorLog.codeBody =
                SahhaConverterUtility.requestBodyArrayToString(req.body) ?: SahhaErrors.noCodeBody
            sahhaErrorLog.codeMethod = req.method
            sahhaErrorLog.codePath = req.url.encodedPath
        }

        sahhaErrorLog.errorLocation = getErrorLocation(response.errorBody())
        sahhaErrorLog.errorCode = response.code()
        sahhaErrorLog.errorMessage = response.message()
        sahhaErrorLog.errorBody = readErrorBody(response.errorBody())
    }

    @JvmName("setApiLogPropertiesDemographicDto")
    private fun setApiLogProperties(
        call: Call<DemographicDto>?,
        location: String,
        code: Int?,
        message: String,
        errorBody: ResponseBody?
    ) {
        sahhaErrorLog.errorSource = API_ERROR
        call?.also {
            sahhaErrorLog.codeBody =
                SahhaConverterUtility.requestBodyToString(it.request().body)
                    ?: SahhaErrors.noCodeBody
            sahhaErrorLog.codeMethod = it.request().method
            sahhaErrorLog.codePath = it.request().url.encodedPath
        }
        sahhaErrorLog.errorLocation = location
        code?.also { sahhaErrorLog.errorCode = it }
        sahhaErrorLog.errorMessage = message
        sahhaErrorLog.errorBody = readErrorBody(errorBody)
    }

    private fun getErrorLocation(errorBody: ResponseBody?): String? {
        return SahhaConverterUtility.responseBodyToSahhaResponseError(
            errorBody
        )?.location
    }

    private fun readErrorBody(errorBody: ResponseBody?): String? {
        val stream = errorBody?.charStream()
        val errorBodyString = stream?.readText()
        stream?.closeQuietly()

        return errorBodyString
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
            null
        )
    }
}