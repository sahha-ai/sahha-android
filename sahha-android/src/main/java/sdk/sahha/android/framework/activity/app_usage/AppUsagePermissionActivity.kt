package sdk.sahha.android.framework.activity.app_usage

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.presentation.usage_stats.AppUsagePrompt
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import java.util.ArrayDeque
import java.util.Queue

internal class AppUsagePermissionActivity : ComponentActivity() {
    private val permissionHandler: PermissionHandler = Sahha.di.permissionHandler
    private val permissionManager: PermissionManager = Sahha.di.permissionManager
    private val queue: Queue<Boolean> = ArrayDeque()

    override fun onStart() {
        super.onStart()

        if (queue.isNotEmpty()) {
            val status = permissionManager.getAppUsageStatus(this)
            permissionHandler.activityCallback.statusCallback?.invoke(
                null, status
            )
            queue.poll()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageManager: PackageManager = packageManager
        val applicationInfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        val appIcon = applicationInfo.loadIcon(packageManager)

        val status = permissionManager.getAppUsageStatus(this)
        val notYetGranted = status != SahhaSensorStatus.enabled

        setContent {
            MaterialTheme(colors = MaterialTheme.colors) {
                var promptVisible by remember { mutableStateOf(notYetGranted) }

                AppUsagePrompt(
                    appName = appName,
                    appIcon = appIcon,
                    visible = promptVisible,
                    onDismiss = {
                        promptVisible = false
                        permissionHandler.activityCallback.statusCallback?.invoke(
                            null,
                            permissionManager.getAppUsageStatus(this@AppUsagePermissionActivity)
                        )
                        finish()
                    },
                    onSettings = {
                        val settingsIntent =
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(settingsIntent)
                        queue.add(true)
                    }
                )
            }
        }
    }
}