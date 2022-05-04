package sdk.sahha.android.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import sdk.sahha.android.SahhaPermissionActivity
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.domain.model.callbacks.ActivityCallback
import sdk.sahha.android.domain.repository.PermissionsRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaActivityStatus

class PermissionsRepoImpl : PermissionsRepo {
    private lateinit var permission: ActivityResultLauncher<String>
    private val activityCallback = ActivityCallback()

    override fun testNewActivate(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaActivityStatus>) -> Unit)
    ) {
        Sahha.motion.activityCallback.requestPermission = callback
        context.startActivity(Intent(context, SahhaPermissionActivity::class.java))
    }

    override fun setPermissionLogic(activity: ComponentActivity) {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
                val status = convertToActivityStatus(enabled)
                activityCallback.requestPermission?.let { it(null, status) }
            }
    }

    override fun openAppSettings(
        context: Context,
    ) {
        val openSettingsIntent = SahhaIntents.settings(context)
        context.startActivity(openSettingsIntent)
    }

    override fun activate(callback: ((error: String?, status: Enum<SahhaActivityStatus>) -> Unit)) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val status = SahhaActivityStatus.unavailable
            callback(SahhaErrors.androidVersionTooLow(9), status)
            Sahha.motion.activityStatus = status
            return
        }

        try {
            activityCallback.requestPermission = callback
            permission.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
        } catch (e: Exception) {
            callback(e.message ?: SahhaErrors.activityNotPrepared, SahhaActivityStatus.pending)
        }
    }

    private fun convertToActivityStatus(enabled: Boolean): Enum<SahhaActivityStatus> {
        if (enabled) return SahhaActivityStatus.enabled
        else return SahhaActivityStatus.disabled
    }
}