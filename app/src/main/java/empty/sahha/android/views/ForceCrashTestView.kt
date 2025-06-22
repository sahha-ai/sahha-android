package empty.sahha.android.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ForceCrashTestView() {
    Spacer(modifier = Modifier.padding(16.dp))
    Button(onClick = {
        throw Exception("Crash test!")
    }) {
        Text("Force Crash Test")
    }
}