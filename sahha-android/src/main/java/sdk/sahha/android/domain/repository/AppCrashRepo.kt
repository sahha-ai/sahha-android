package sdk.sahha.android.domain.repository

import android.os.Build
import androidx.annotation.RequiresApi
import sdk.sahha.android.domain.model.error_log.AppCrashInfo

internal interface AppCrashRepo {
    @RequiresApi(Build.VERSION_CODES.R)
    fun getAppExitInfo(packageName: String): List<AppCrashInfo>
}