package empty.sahha.android.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaBiomarkerCategory
import sdk.sahha.android.source.SahhaBiomarkerType

@Composable
fun BiomarkersView() {
    var result by remember { mutableStateOf("Pending") }

    fun setResultNoBiomarkers() {
        result = "No biomarkers"
    }

    Button(
        onClick = {
            result = "Loading..."
            Sahha.getBiomarkers(
                categories = SahhaBiomarkerCategory.values().toSet(),
                types = SahhaBiomarkerType.values().toSet(),
            ) { error, value ->
                error?.also { result = it }
                    ?: value?.also { result = it }
                    ?: setResultNoBiomarkers()
            }
        }
    ) {
        Text("Get Biomarkers")
    }

    Spacer(modifier = Modifier.size(8.dp))
    Text(result)
    Spacer(modifier = Modifier.size(8.dp))
}