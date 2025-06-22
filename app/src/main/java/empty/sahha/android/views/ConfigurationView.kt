package empty.sahha.android.views

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSettings

@Composable
fun ConfigurationView(activity: ComponentActivity, config: SahhaSettings) {
    val coroutineScope = rememberCoroutineScope()

    Button(onClick = {
        Sahha.configure(activity, config) { error, success ->
            coroutineScope.launch {
                Toast.makeText(
                    activity,
                    error ?: "Successful $success",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }) {
        Text("Configure")
    }
}