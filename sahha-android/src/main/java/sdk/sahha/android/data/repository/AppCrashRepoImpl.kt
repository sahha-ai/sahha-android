package sdk.sahha.android.data.repository

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import sdk.sahha.android.domain.model.error_log.AppCrashInfo
import sdk.sahha.android.domain.model.error_log.toAppCrashInfo
import sdk.sahha.android.domain.repository.AppCrashRepo
import javax.inject.Inject

internal class AppCrashRepoImpl @Inject constructor(
    private val activityManager: ActivityManager,
) : AppCrashRepo {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun getAppExitInfo(
        packageName: String
    ): List<AppCrashInfo> {
        val info = activityManager.getHistoricalProcessExitReasons(packageName, 0, 0)
        return info.map { it.toAppCrashInfo() }
    }
}