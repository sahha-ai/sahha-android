package sdk.sahha.android.activity.health_connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import sdk.sahha.android.ui.theme.SahhasdkemptyTheme

class SahhaHealthConnectPermissionActivity : ComponentActivity() {
    private val permissionHandler by lazy { Sahha.di.permissionHandler }
    private val healthConnectClient by lazy { Sahha.di.healthConnectClient }

    private val permissions =
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepStageRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        )

    // Create the permissions launcher.
    private val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    private val requestPermissions =
        registerForActivityResult(requestPermissionActivityContract) { granted ->
            if (granted.containsAll(permissions)) {
                // Permissions successfully granted
                // PERMISSIONS: Set<string> as of Alpha11
                permissionHandler.activityCallback.statusCallback
                    ?.invoke(null, SahhaSensorStatus.enabled)
            } else {
                // Lack of required permissions
                permissionHandler.activityCallback.statusCallback
                    ?.invoke(null, SahhaSensorStatus.disabled)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        healthConnectClient?.also { client ->
            lifecycleScope.launch {
                checkPermissionsAndRun(client)
            }
        } ?: finish()

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

    private suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(permissions)) {
            // Permissions already granted; proceed with inserting or reading data.
            permissionHandler.activityCallback.statusCallback
                ?.invoke(null, SahhaSensorStatus.enabled)
        } else {
            permissionHandler.activityCallback.statusCallback
                ?.invoke(null, SahhaSensorStatus.disabled)
            requestPermissions.launch(permissions)
        }
        finish()
    }
}