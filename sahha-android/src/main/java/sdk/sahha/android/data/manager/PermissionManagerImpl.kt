package sdk.sahha.android.data.manager

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import sdk.sahha.android.SahhaPermissionActivity
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject

private val tag = "PermissionManagerImpl"
class PermissionManagerImpl @Inject constructor(
    private val permissionHandler: PermissionHandler,
    private val sahhaErrorLogger: SahhaErrorLogger
) : PermissionManager {
    private lateinit var permission: ActivityResultLauncher<String>

    override fun setPermissionLogic(activity: ComponentActivity) {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
                val status = convertToActivityStatus(enabled)
                permissionHandler.activityCallback.requestPermission?.let { it(null, status) }
            }
    }

    override fun openAppSettings(
        context: Context,
    ) {
        val openSettingsIntent = SahhaIntents.settings(context)
        context.startActivity(openSettingsIntent)
    }

    override fun activate(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val status = SahhaSensorStatus.unavailable
            callback(SahhaErrors.androidVersionTooLow(9), status)
            permissionHandler.sensorStatus = status
            return
        }

        try {
            permissionHandler.activityCallback.requestPermission = callback
            val intent = Intent(context, SahhaPermissionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            callback(e.message, SahhaSensorStatus.pending)

            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                tag,
                "activate",
                e.stackTraceToString()
            )
        }
    }

    private fun convertToActivityStatus(enabled: Boolean): Enum<SahhaSensorStatus> {
        if (enabled) return SahhaSensorStatus.enabled
        else return SahhaSensorStatus.disabled
    }

    override fun enableSensors(
        context: Context,
        callback: (error: String?, status: Enum<SahhaSensorStatus>) -> Unit
    ) {
        SahhaPermissions.enableSensor(context) {
            callback(null, it)
            Sahha.sim.start()
        }
    }

    override fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        SahhaPermissions.getSensorStatus(context) {
            callback(null, it)
        }
    }
}