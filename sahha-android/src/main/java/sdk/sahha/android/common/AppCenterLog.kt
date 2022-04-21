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
import sdk.sahha.android.data.Constants.PLATFORM_NAME
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
        eventName: String,
        auth: Boolean,
        call: Call<ResponseBody>?,
        type: String,
    ) {
        defaultScope.launch {
            properties.clear()
            setStaticProperties()
            setApiLogProperties(auth, call, type)
            Analytics.trackEvent(eventName, properties)
        }
    }

    fun application(error: String) {
        defaultScope.launch {
            properties.clear()
            setStaticProperties()
            Analytics.trackEvent(error)
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
            put(API_ERROR, type)
        }
    }

    @SuppressLint("HardwareIds")
    private suspend fun setStaticProperties() {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appId = packageInfo.packageName
        val versionName: String? = packageInfo.versionName
        val config = configurationDao.getConfig()

        properties["sdk_id"] = config.framework
        properties["sdk_version"] = sdk.sahha.android.BuildConfig.SAHHA_SDK_VERSION
        properties["app_id"] = appId
        properties["app_version"] = versionName ?: "Unknown"
        properties["device_type"] = Build.MANUFACTURER
        properties["device_model"] = Build.MODEL
        properties["system"] = PLATFORM_NAME
        properties["system_version"] =
            "Android SDK: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})"
    }
}