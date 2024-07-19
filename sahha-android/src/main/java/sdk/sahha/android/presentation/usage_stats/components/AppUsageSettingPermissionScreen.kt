package sdk.sahha.android.presentation.usage_stats.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap


@Preview
@Composable
fun AppUsageSettingPermissionScreenPreview() {
    AppUsageSettingPermissionScreen(
        "ExampleApp",
        null
    ) {}
}

@Composable
fun AppUsageSettingPermissionScreen(
    appName: String,
    appIcon: Drawable?,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.03f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Box(
            modifier = Modifier.padding(10.dp)
        ) {
            Column {
//                Text(text = "Usage access", fontSize = 32.sp)
//                Spacer(modifier = Modifier.size(20.dp))
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    appIcon?.also { icon ->
//                        Image(
//                            bitmap = icon.toBitmap().asImageBitmap(),
//                            contentDescription = "App Icon",
//                            modifier = Modifier.size(50.dp)
//                        )
//                    }
//                    Spacer(modifier = Modifier.size(5.dp))
//                    Text(text = appName, fontSize = 18.sp, fontWeight = FontWeight.Medium)
//                    Spacer(modifier = Modifier.size(5.dp))
//                    Text(text = "1.0", color = Color.Gray)
//                }
//                Spacer(modifier = Modifier.size(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Permit usage access", fontSize = 18.sp)
                    Switch(
                        checked = true, onCheckedChange = { onToggle() }, enabled = true,
                        colors = SwitchDefaults.colors(
                            disabledCheckedThumbColor = Color.White,
                            disabledCheckedTrackColor = MaterialTheme.colorScheme.primary,
                        )
                    )
                }
            }
        }
    }
}