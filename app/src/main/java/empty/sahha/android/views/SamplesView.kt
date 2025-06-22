package empty.sahha.android.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import java.time.LocalDateTime

@Composable
fun SamplesView() {
    var result by remember { mutableStateOf("Select a sensor to view samples") }
    var isLoading by remember { mutableStateOf(false) }
    var currentSensor by remember { mutableStateOf<SahhaSensor?>(null) }

    // Create Gson instance for JSON formatting
    val gson = remember {
        GsonBuilder()
            .setPrettyPrinting()
            .create()
    }

    // Filter out deprecated sensors for cleaner UI
    val availableSensors = SahhaSensor.values().filterNot { sensor ->
        // You can customize this filter based on which sensors you want to show
        // For now, keeping all non-deprecated ones
        sensor.name.contains("deprecated", ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Current sensor display
        if (currentSensor != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Current Sensor: ${currentSensor?.name}",
                        style = MaterialTheme.typography.h6
                    )
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Sensor buttons in a scrollable list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp), // Fixed height to ensure results section is always visible
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(availableSensors) { sensor ->
                Button(
                    onClick = {
                        currentSensor = sensor
                        result = "Loading samples for ${sensor.name}..."
                        isLoading = true

                        try {
                            Sahha.getSamples(
                                sensor,
                                Pair(
                                    LocalDateTime.now().minusDays(7),
                                    LocalDateTime.now()
                                )
                            ) { error, samples ->
                                isLoading = false
                                result = if (error != null) {
                                    "Error: $error"
                                } else if (samples.isNullOrEmpty()) {
                                    "No samples found for ${sensor.name}"
                                } else {
                                    buildString {
                                        append("Samples for ${sensor.name}:\n\n")
                                        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
                                        samples.forEach { sample ->
                                            scope.launch {
                                                append("${gson.toJson(sample)}\n\n")
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            result = "Exception: ${e.message}"
                            println(e.message)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (currentSensor == sensor) {
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.surface
                        }
                    )
                ) {
                    Text(
                        text = sensor.name.replace("_", " ").capitalize(),
                        style = MaterialTheme.typography.body2,
                        color = if (currentSensor == sensor) {
                            MaterialTheme.colors.onPrimary
                        } else {
                            MaterialTheme.colors.onSurface
                        }
                    )
                }
            }
        }

        // Results display - scrollable
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Results:",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }
}