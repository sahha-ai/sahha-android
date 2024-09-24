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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sdk.sahha.android.domain.internal_enum.RationaleSensorType
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.presentation.usage_stats.AppUsagePrompt
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import java.util.ArrayDeque
import java.util.Queue

internal class AppUsagePermissionActivity : ComponentActivity() {
    private val permissionManager: PermissionManager = Sahha.di.permissionManager
    private val rationaleManager = Sahha.di.rationaleManager
    private val queue: Queue<Boolean> = ArrayDeque()

    override fun onStart() {
        super.onStart()

        if (queue.isNotEmpty()) {
            val status = permissionManager.getAppUsageStatus(this)
            lifecycleScope.launch {
                updateRationale(status)
                val updatedStatus = permissionManager.getAppUsageStatus(this@AppUsagePermissionActivity)
                permissionManager.appUsageCallback.invoke(
                    null, updatedStatus
                )
                queue.poll()
                finish()
            }
        }
    }

    private suspend fun updateRationale(status: Enum<SahhaSensorStatus>) {
        val rationale = rationaleManager.getRationale(RationaleSensorType.APP_USAGE)

        if (status == SahhaSensorStatus.enabled)
            rationaleManager.removeRationales(
                listOf(rationale)
            )

        rationaleManager.saveRationale(
            rationale.copy(denialCount = rationale.denialCount + 1)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageManager: PackageManager = packageManager
        val applicationInfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        val appIcon = applicationInfo.loadIcon(packageManager)

        val usageStatus = permissionManager.getAppUsageStatus(this)
        val notYetGranted = usageStatus != SahhaSensorStatus.enabled

        setContent {
            MaterialTheme(colors = MaterialTheme.colors) {
                var promptVisible by remember { mutableStateOf(notYetGranted) }

                AppUsagePrompt(
                    appName = appName,
                    appIcon = appIcon,
                    visible = promptVisible,
                    onDismiss = {
                        promptVisible = false
                        val status =
                            permissionManager.getAppUsageStatus(this@AppUsagePermissionActivity)
                        lifecycleScope.launch {
                            updateRationale(status)
                            val updatedStatus = permissionManager.getAppUsageStatus(this@AppUsagePermissionActivity)
                            permissionManager.appUsageCallback.invoke(
                                null,
                                updatedStatus
                            )
                            finish()
                        }
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