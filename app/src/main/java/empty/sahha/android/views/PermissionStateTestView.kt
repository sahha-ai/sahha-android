package empty.sahha.android.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor

@Composable
fun PermissionStateTestView() {
    val context = LocalContext.current
    var permissionStatus by remember { mutableStateOf("none") }

    Column {
        Text(text = permissionStatus)
        Spacer(modifier = Modifier.padding(8.dp))

        Button(onClick = {
            Sahha.getSensorStatus(
                context,
                setOf<SahhaSensor>(SahhaSensor.device_lock)
            ) { error, status ->
                permissionStatus = "${status.name}${error?.let { "\n$it" } ?: ""}"
            }
        }) {
            Text("Device Lock")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(onClick = {
            Sahha.getSensorStatus(
                context,
                setOf<SahhaSensor>(SahhaSensor.steps)
            ) { error, status ->
                permissionStatus = "${status.name}${error?.let { "\n$it" } ?: ""}"
            }
        }) {
            Text("Step Count")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(onClick = {
            Sahha.getSensorStatus(
                context,
                setOf<SahhaSensor>(SahhaSensor.sleep)
            ) { error, status ->
                permissionStatus = "${status.name}${error?.let { "\n$it" } ?: ""}"
            }
        }) {
            Text("Sleep")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(onClick = {
            Sahha.getSensorStatus(
                context,
                setOf<SahhaSensor>(SahhaSensor.heart_rate)
            ) { error, status ->
                permissionStatus = "${status.name}${error?.let { "\n$it" } ?: ""}"
            }
        }) {
            Text("Heart Rate")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(onClick = {
            Sahha.getSensorStatus(
                context,
                setOf<SahhaSensor>(SahhaSensor.heart_rate_variability_sdnn)
            ) { error, status ->
                permissionStatus = "${status.name}${error?.let { "\n$it" } ?: ""}"
            }
        }) {
            Text("Heart Rate Var Sdnn")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(onClick = {
            Sahha.getSensorStatus(
                context,
                setOf<SahhaSensor>(
                    SahhaSensor.device_lock,
                    SahhaSensor.steps,
                    SahhaSensor.sleep,
                    SahhaSensor.heart_rate,
                    SahhaSensor.heart_rate_variability_sdnn
                )
            ) { error, status ->
                permissionStatus = "${status.name}${error?.let { "\n$it" } ?: ""}"
            }
        }) {
            Text("Grouped")
        }

        Spacer(modifier = Modifier.padding(8.dp))
    }
}