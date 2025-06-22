package empty.sahha.android.views

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor

@Composable
fun SensorStatusView(context: Context, sensors: Set<SahhaSensor>) {
    var permissionStatus by remember { mutableStateOf("") }
    val mainScope = remember { CoroutineScope(Dispatchers.Main) }

    // Initial sensor status check
    LaunchedEffect(Unit) {
        Sahha.getSensorStatus(context, sensors) { error, sensorStatus ->
            mainScope.launch {
                permissionStatus = "${sensorStatus}${error?.let { "\n$it" } ?: ""}"
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status Display Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Sensor Status:",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (permissionStatus.isNotEmpty()) permissionStatus else "Loading...",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }

        // Status Check Buttons Section
        item {
            Text(
                text = "Check Sensor Status:",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Button(
                        onClick = {
                            Sahha.getSensorStatus(context, setOf<SahhaSensor>()) { error, status ->
                                permissionStatus = "${status}${error?.let { "\n$it" } ?: ""}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("No Sensors", style = MaterialTheme.typography.caption)
                    }
                }

                item {
                    Button(
                        onClick = {
                            Sahha.getSensorStatus(
                                context,
                                setOf(SahhaSensor.heart_rate, SahhaSensor.sleep)
                            ) { error, status ->
                                permissionStatus = "${status}${error?.let { "\n$it" } ?: ""}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Some Sensors", style = MaterialTheme.typography.caption)
                    }
                }

                item {
                    Button(
                        onClick = {
                            Sahha.getSensorStatus(context, SahhaSensor.values().toSet()) { error, status ->
                                permissionStatus = "${status}${error?.let { "\n$it" } ?: ""}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("All Sensors", style = MaterialTheme.typography.caption)
                    }
                }

                item {
                    Button(
                        onClick = {
                            Sahha.getSensorStatus(context, sensors) { error, status ->
                                permissionStatus = "${status}${error?.let { "\n$it" } ?: ""}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Current Sensors", style = MaterialTheme.typography.caption)
                    }
                }
            }
        }

        // Permission Grant Buttons Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Grant Permissions:",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            // Filter out deprecated iOS-only sensors for Android
            val androidSensors = SahhaSensor.values().filter { sensor ->
                // Include sensors that are not deprecated or are Android-compatible
                try {
                    val field = SahhaSensor::class.java.getField(sensor.name)
                    val deprecated = field.getAnnotation(Deprecated::class.java)
                    deprecated == null || !deprecated.message.contains("IOS_ONLY")
                } catch (e: Exception) {
                    true // If we can't check, include it
                }
            }

            // Calculate grid height based on number of sensors (All Permissions + Android sensors)
            val totalItems = 1 + androidSensors.size
            val rows = (totalItems + 1) / 2 // Round up for 2 columns
            val gridHeight = (rows * 56).dp // Approximate button height

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(gridHeight),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Permissions button first
                item {
                    Button(
                        onClick = {
                            Sahha.enableSensors(context, androidSensors.toSet()) { error, status ->
                                permissionStatus = "${status}${error?.let { "\n$it" } ?: ""}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("All Android Permissions", style = MaterialTheme.typography.caption)
                    }
                }

                // Individual sensor permission buttons (Android-compatible only)
                items(androidSensors.size) { index ->
                    val sensor = androidSensors[index]
                    Button(
                        onClick = {
                            Sahha.enableSensors(context, setOf(sensor)) { error, status ->
                                permissionStatus = "${status}${error?.let { "\n$it" } ?: ""}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                    ) {
                        Text(
                            text = sensor.name.replace("_", " ").lowercase()
                                .split(" ")
                                .joinToString(" ") { word ->
                                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                },
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}