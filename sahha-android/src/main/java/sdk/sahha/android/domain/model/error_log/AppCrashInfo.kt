package sdk.sahha.android.domain.model.error_log

import android.app.ApplicationExitInfo
import android.os.Build
import androidx.annotation.RequiresApi

internal data class AppCrashInfo(
    val reason: Int,
    val description: String,
    val timestamp: Long,
)

@RequiresApi(Build.VERSION_CODES.R)
internal fun ApplicationExitInfo.toAppCrashInfo(): AppCrashInfo {
    ApplicationExitInfo.REASON_EXIT_SELF
    return AppCrashInfo(
        reason = this.reason,
        description = this.description ?: "Something went wrong",
        timestamp = this.timestamp
    )
}

internal enum class CrashReason(private val reason: Int) {
    REASON_ANR(6),
    REASON_CRASH(4),
    REASON_CRASH_NATIVE(5),
    REASON_DEPENDENCY_DIED(12),
    REASON_EXCESSIVE_RESOURCE_USAGE(9),
    REASON_EXIT_SELF(1),
    REASON_FREEZER(14),
    REASON_INITIALIZATION_FAILURE(7),
    REASON_LOW_MEMORY(3),
    REASON_OTHER(13),
    REASON_PACKAGE_STATE_CHANGE(15),
    REASON_PACKAGE_UPDATED(16),
    REASON_PERMISSION_CHANGE(8),
    REASON_SIGNALED(2),
    REASON_UNKNOWN(0),
    REASON_USER_REQUESTED(10),
    REASON_USER_STOPPED(11);

    companion object {
        fun fromReason(reason: Int): CrashReason? {
            return values().find { it.reason == reason }
        }
    }
}