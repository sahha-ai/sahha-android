package sdk.sahha.android.activity.health_connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import sdk.sahha.android.ui.theme.SahhasdkemptyTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

internal class SahhaHealthConnectStatusActivity : ComponentActivity() {
    private val permissionHandler by lazy { Sahha.di.permissionHandler }
    private val healthConnectClient by lazy { Sahha.di.healthConnectClient }
    private val healthConnectRepo by lazy { Sahha.di.healthConnectRepo }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        healthConnectClient?.also { client ->
            lifecycleScope.launch {
                checkPermissions(client)
            }
        } ?: healthConnectUnavailable()

        setContent {
            SahhasdkemptyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background.copy(alpha = 0f),
                ) {}
            }
        }
    }

    private fun atleastOnePermissionGranted(granted: Set<String>): Boolean {
        return granted.any { it in healthConnectRepo.permissions }
    }

    private suspend fun checkPermissions(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()

        if (atleastOnePermissionGranted(granted)) {
            permissionHandler.activityCallback.statusCallback
                ?.invoke(null, SahhaSensorStatus.enabled)
            Sahha.di.sahhaAlarmManager.setAlarm(
                this,
                Instant.now()
                    .plus(10, ChronoUnit.SECONDS)
                    .toEpochMilli()
            )
            finish()
            return
        }

        // Else
        permissionHandler.activityCallback.statusCallback
            ?.invoke(null, SahhaSensorStatus.disabled)
        finish()
    }

    private fun healthConnectUnavailable() {
        permissionHandler.activityCallback.statusCallback
            ?.invoke(null, SahhaSensorStatus.unavailable)
        finish()
    }
}