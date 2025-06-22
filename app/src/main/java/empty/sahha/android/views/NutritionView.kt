package empty.sahha.android.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.metadata.Metadata as HealthMetadata
import androidx.health.connect.client.units.Energy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime

@Composable
fun NutritionView(context: Context) {
    var caloriesInput by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Ready to add energy consumed data") }
    var isLoading by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    var healthConnectAvailable by remember { mutableStateOf(true) }
    val mainScope = remember { CoroutineScope(Dispatchers.Main) }

    val healthConnectClient = remember {
        try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            null
        }
    }

    // Health Connect permissions
    val permissions = setOf(HealthPermission.getWritePermission(NutritionRecord::class))

    // Different permission handling for different Android versions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        hasPermission = granted.containsAll(permissions)
        if (hasPermission) {
            result = "Permissions granted! Ready to add energy data."
        } else {
            result = "Permissions denied. Cannot add data to Health Connect."
        }
    }

    // Intent launcher for manual Health Connect settings
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Check permissions again after returning from settings
        mainScope.launch {
            try {
                healthConnectClient?.let { client ->
                    val grantedPermissions = client.permissionController.getGrantedPermissions()
                    hasPermission = grantedPermissions.containsAll(permissions)
                    result = if (hasPermission) {
                        "Permissions granted! Ready to add energy data."
                    } else {
                        "Please grant nutrition permissions in Health Connect settings."
                    }
                }
            } catch (e: Exception) {
                result = "Error checking permissions: ${e.message}"
            }
        }
    }

    // Check permissions and Health Connect availability on start
    LaunchedEffect(Unit) {
        try {
            if (healthConnectClient == null) {
                healthConnectAvailable = false
                result = "Health Connect is not available on this device."
                return@LaunchedEffect
            }

            // Check Health Connect availability
            when (HealthConnectClient.getSdkStatus(context)) {
                HealthConnectClient.SDK_UNAVAILABLE -> {
                    healthConnectAvailable = false
                    result = "Health Connect is not available on this device."
                    return@LaunchedEffect
                }
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    healthConnectAvailable = false
                    result = "Health Connect requires an update. Please update from Play Store."
                    return@LaunchedEffect
                }
                HealthConnectClient.SDK_AVAILABLE -> {
                    healthConnectAvailable = true
                }
            }

            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            hasPermission = grantedPermissions.containsAll(permissions)
            if (hasPermission) {
                result = "Permissions already granted. Ready to add energy data."
            } else {
                result = "Need to request Health Connect permissions for nutrition data."
            }
        } catch (e: Exception) {
            result = "Error checking Health Connect: ${e.message}"
            healthConnectAvailable = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Add Energy Consumed",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add energy consumed entries directly to Health Connect",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Permission Section
        if (!healthConnectAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Health Connect Unavailable",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = result,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        } else if (!hasPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Permissions Required",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Health Connect permission is required to write nutrition data. This will open Health Connect permissions.",
                        style = MaterialTheme.typography.body2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    // Android 14+ - try the permission contract first
                                    permissionLauncher.launch(permissions)
                                } else {
                                    // Android 13 and below - open Health Connect settings manually
                                    val intent = Intent().apply {
                                        action = "androidx.health.ACTION_HEALTH_CONNECT_SETTINGS"
                                        if (context.packageManager.resolveActivity(this, 0) == null) {
                                            // Fallback to package-specific Health Connect
                                            data = Uri.parse("package:com.google.android.apps.healthdata")
                                            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        }
                                    }
                                    settingsLauncher.launch(intent)
                                }
                            } catch (e: Exception) {
                                result = "Error opening Health Connect: ${e.message}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                "Request Health Connect Permissions"
                            } else {
                                "Open Health Connect Settings"
                            }
                        )
                    }
                }
            }
        }

        // Input Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Energy Amount:",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = caloriesInput,
                        onValueChange = { caloriesInput = it },
                        label = { Text("Calories") },
                        placeholder = { Text("Enter calories consumed") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && hasPermission && healthConnectAvailable,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "kcal",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val calories = caloriesInput.toDoubleOrNull()
                            if (calories != null && calories > 0) {
                                isLoading = true
                                result = "Adding energy consumed entry..."

                                mainScope.launch {
                                    try {
                                        if (healthConnectClient == null) {
                                            isLoading = false
                                            result = "Health Connect client not available"
                                            return@launch
                                        }

                                        val now = Instant.now()
                                        val nutritionRecord = NutritionRecord(
                                            startTime = now.minusSeconds(60),
                                            endTime = now,
                                            startZoneOffset = ZonedDateTime.now().offset,
                                            endZoneOffset = ZonedDateTime.now().offset,
                                            energy = Energy.kilocalories(calories),
                                            metadata = HealthMetadata()
                                        )

                                        healthConnectClient.insertRecords(listOf(nutritionRecord))

                                        isLoading = false
                                        result = "Successfully added $calories kcal to Health Connect!"
                                        caloriesInput = "" // Clear input on success

                                    } catch (e: Exception) {
                                        isLoading = false
                                        result = "Error: ${e.message}"
                                    }
                                }
                            } else {
                                result = "Please enter a valid number of calories (greater than 0)"
                            }
                        },
                        enabled = !isLoading && caloriesInput.isNotBlank() && hasPermission && healthConnectAvailable,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colors.onPrimary
                            )
                        } else {
                            Text("Add Entry")
                        }
                    }

                    Button(
                        onClick = {
                            caloriesInput = ""
                            result = "Ready to add energy consumed data"
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
                    ) {
                        Text("Clear")
                    }
                }
            }
        }

        // Quick Entry Buttons
        if (hasPermission && healthConnectAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Entry:",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val quickValues = listOf(100, 250, 500, 750)
                        quickValues.forEach { calories ->
                            Button(
                                onClick = { caloriesInput = calories.toString() },
                                enabled = !isLoading && healthConnectAvailable,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.secondary
                                )
                            ) {
                                Text(
                                    text = "$calories",
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }
                }
            }
        }

        // Status Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = result,
                    style = MaterialTheme.typography.body2,
                    color = when {
                        result.contains("Error") -> MaterialTheme.colors.error
                        result.contains("Successfully") -> MaterialTheme.colors.primary
                        result.contains("denied") -> MaterialTheme.colors.error
                        result.contains("granted") -> MaterialTheme.colors.primary
                        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
        }

        // Info Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ℹ️ Information",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "This will add nutrition data directly to Health Connect. The entry will be timestamped with the current time and require WRITE_NUTRITION permission.",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}