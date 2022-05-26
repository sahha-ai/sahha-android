package sdk.sahha.android.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import com.microsoft.appcenter.analytics.Analytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import sdk.sahha.android.data.Constants.API_AUTH
import sdk.sahha.android.data.Constants.API_BODY
import sdk.sahha.android.data.Constants.API_ERROR
import sdk.sahha.android.data.Constants.API_METHOD
import sdk.sahha.android.data.Constants.API_URL
import sdk.sahha.android.data.Constants.APPLICATION_ERROR
import sdk.sahha.android.data.Constants.APP_ID
import sdk.sahha.android.data.Constants.APP_VERSION
import sdk.sahha.android.data.Constants.DEVICE_MODEL
import sdk.sahha.android.data.Constants.DEVICE_TYPE
import sdk.sahha.android.data.Constants.ERROR_MESSAGE
import sdk.sahha.android.data.Constants.ERROR_SOURCE
import sdk.sahha.android.data.Constants.ERROR_TYPE
import sdk.sahha.android.data.Constants.PLATFORM_NAME
import sdk.sahha.android.data.Constants.SDK_ID
import sdk.sahha.android.data.Constants.SDK_VERSION
import sdk.sahha.android.data.Constants.SYSTEM
import sdk.sahha.android.data.Constants.SYSTEM_VERSION
import sdk.sahha.android.data.local.dao.ConfigurationDao
import javax.inject.Inject
import javax.inject.Named

class AppCenterLog @Inject constructor(
    private val context: Context,
    private val configurationDao: ConfigurationDao,
    @Named("defaultScope") private val defaultScope: CoroutineScope
) {
    private val properties = hashMapOf<String, String>()

    fun api(
        auth: Boolean,
        call: Call<ResponseBody>?,
        type: String,
    ) {
        defaultScope.launch {
            properties.clear()
            setStaticProperties()
            setApiLogProperties(auth, call, type)
            Analytics.trackEvent(API_ERROR, properties)
        }
    }

    fun application(
        errorSource: String,
        error: String
    ) {
        defaultScope.launch {
            properties.clear()
            setStaticProperties()
            setApplicationLogProperties(errorSource, error)
            Analytics.trackEvent(APPLICATION_ERROR, properties)
        }
    }

    private fun setApplicationLogProperties(errorSource: String, error: String) {
        properties.apply {
            put(ERROR_SOURCE, errorSource)
            put(ERROR_MESSAGE, error)
        }
    }

    private fun setApiLogProperties(
        auth: Boolean,
        call: Call<ResponseBody>?,
        type: String
    ) {
        properties.apply {
            put(API_AUTH, auth.toString())
            call?.also {
                put(
                    API_BODY,
                    ApiBodyConverter.requestBodyToString(it.request().body) ?: "No data found"
                )
                put(API_METHOD, it.request().method)
                put(API_URL, it.request().url.encodedPath)
            }
            put(ERROR_TYPE, type)
        }
    }

    @SuppressLint("HardwareIds")
    private suspend fun setStaticProperties() {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appId = packageInfo.packageName
        val versionName: String? = packageInfo.versionName
        val config = configurationDao.getConfig()

        properties[SDK_ID] = config.framework
        properties[SDK_VERSION] = sdk.sahha.android.BuildConfig.SAHHA_SDK_VERSION
        properties[APP_ID] = appId
        properties[APP_VERSION] = versionName ?: "Unknown"
        properties[DEVICE_TYPE] = Build.MANUFACTURER
        properties[DEVICE_MODEL] = Build.MODEL
        properties[SYSTEM] = PLATFORM_NAME
        properties[SYSTEM_VERSION] =
            "Android SDK: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})"
    }
}