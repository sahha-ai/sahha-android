package sdk.sahha.android.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.data.Constants.API_ERROR
import sdk.sahha.android.data.Constants.APPLICATION_ERROR
import sdk.sahha.android.data.Constants.PLATFORM_NAME
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.data.remote.SahhaErrorApi
import sdk.sahha.android.data.remote.dto.DemographicDto
import sdk.sahha.android.domain.model.error_log.SahhaErrorLog
import javax.inject.Inject
import javax.inject.Named

class SahhaErrorLogger @Inject constructor(
    private val context: Context,
    private val configurationDao: ConfigurationDao,
    private val decryptor: Decryptor,
    private val sahhaErrorApi: SahhaErrorApi,
    @Named("defaultScope") private val defaultScope: CoroutineScope
) {
    private var sahhaErrorLog = getNewSahhaErrorLog()

    fun api(
        call: Call<ResponseBody>,
        type: String,
        code: Int?,
        message: String
    ) {
        defaultScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(call, type, code, message)
            sahhaErrorApi.postErrorLog(
                decryptor.decrypt(UET),
                sahhaErrorLog
            )
        }
    }

    @JvmName("apiUnit")
    fun api(
        call: Call<Unit>,
        type: String,
        code: Int?,
        message: String
    ) {
        defaultScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(call, type, code, message)
            sahhaErrorApi.postErrorLog(
                decryptor.decrypt(UET),
                sahhaErrorLog
            )
        }
    }

    @JvmName("apiDemographicDto")
    fun api(
        call: Call<DemographicDto>?,
        type: String,
        code: Int?,
        message: String
    ) {
        defaultScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApiLogProperties(call, type, code, message)
            sahhaErrorApi.postErrorLog(
                decryptor.decrypt(UET),
                sahhaErrorLog
            )
        }
    }

    fun application(
        error: String?,
        appMethod: String,
        appBody: String?
    ) {
        defaultScope.launch {
            sahhaErrorLog = getNewSahhaErrorLog()
            setStaticParameters()
            setApplicationLogProperties(error, appMethod, appBody)
        }
    }

    private fun setApplicationLogProperties(error: String?, appMethod: String, appBody: String?) {
        sahhaErrorLog.errorSource = APPLICATION_ERROR
        error?.also { sahhaErrorLog.errorMessage = it }
        sahhaErrorLog.appMethod = appMethod
        appBody?.also { sahhaErrorLog.appBody = it }
    }

    private fun setApiLogProperties(
        call: Call<ResponseBody>?,
        type: String,
        code: Int?,
        message: String
    ) {
        sahhaErrorLog.errorSource = API_ERROR
        call?.also {
            sahhaErrorLog.apiBody =
                ApiBodyConverter.requestBodyToString(it.request().body) ?: SahhaErrors.noData
            sahhaErrorLog.apiMethod = it.request().method
            sahhaErrorLog.apiURL = it.request().url.encodedPath
        }
        sahhaErrorLog.errorType = type
        code?.also { sahhaErrorLog.errorCode = it }
        sahhaErrorLog.errorMessage = message
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
            sahhaErrorLog.apiBody =
                ApiBodyConverter.requestBodyToString(it.request().body) ?: SahhaErrors.noData
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
            sahhaErrorLog.apiBody =
                ApiBodyConverter.requestBodyToString(it.request().body) ?: SahhaErrors.noData
            sahhaErrorLog.apiMethod = it.request().method
            sahhaErrorLog.apiURL = it.request().url.encodedPath
        }
        sahhaErrorLog.errorType = type
        code?.also { sahhaErrorLog.errorCode = it }
        sahhaErrorLog.errorMessage = message
    }

    @SuppressLint("HardwareIds")
    private suspend fun setStaticParameters() {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appId = packageInfo.packageName
        val versionName: String? = packageInfo.versionName
        val config = configurationDao.getConfig()

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