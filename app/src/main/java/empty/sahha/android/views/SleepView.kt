package empty.sahha.android.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.metadata.Metadata as HealthMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

// Data class to hold stage input
data class StageInput(
    var type: Int,
    var startTime: String,
    var endTime: String
)

@Composable
fun SleepView(context: Context) {
    // State variables
    var sessionStartTime by remember { mutableStateOf("") }
    var sessionEndTime by remember { mutableStateOf("") }
    val stages = remember { mutableStateListOf<StageInput>() }
    var result by remember { mutableStateOf("Ready to add sleep data") }
    var isLoading by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    var healthConnectAvailable by remember { mutableStateOf(true) }
    val mainScope = remember { CoroutineScope(Dispatchers.Main) }

    // Health Connect client
    val healthConnectClient = remember {
        try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            null
        }
    }

    // Permissions for SleepSessionRecord
    val permissions = setOf(HealthPermission.getWritePermission(SleepSessionRecord::class))

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        hasPermission = granted.containsAll(permissions)
        result = if (hasPermission) {
            "Permissions granted! Ready to add sleep data."
        } else {
            "Permissions denied. Cannot add data to Health Connect."
        }
    }

    // Settings launcher
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        mainScope.launch {
            try {
                healthConnectClient?.let { client ->
                    val grantedPermissions = client.permissionController.getGrantedPermissions()
                    hasPermission = grantedPermissions.containsAll(permissions)
                    result = if (hasPermission) {
                        "Permissions granted! Ready to add sleep data."
                    } else {
                        "Please grant sleep permissions in Health Connect settings."
                    }
                }
            } catch (e: Exception) {
                result = "Error checking permissions: ${e.message}"
            }
        }
    }

    // Check permissions and availability on start
    LaunchedEffect(Unit) {
        try {
            if (healthConnectClient == null) {
                healthConnectAvailable = false
                result = "Health Connect is not available on this device."
                return@LaunchedEffect
            }

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
            result = if (hasPermission) {
                "Permissions already granted. Ready to add sleep data."
            } else {
                "Need to request Health Connect permissions for sleep data."
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
        // Header card
        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Sleep Data",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add sleep session data with stages to Health Connect",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Permission section
        if (!healthConnectAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Health Connect Unavailable",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = result, style = MaterialTheme.typography.body2)
                }
            }
        } else if (!hasPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Permissions Required",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Health Connect permission is required to write sleep data. This will open Health Connect permissions.",
                        style = MaterialTheme.typography.body2
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    permissionLauncher.launch(permissions)
                                } else {
                                    val intent = Intent().apply {
                                        action = "androidx.health.ACTION_HEALTH_CONNECT_SETTINGS"
                                        if (context.packageManager.resolveActivity(this, 0) == null) {
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

        // Input section
        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sleep Session Details:",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = sessionStartTime,
                    onValueChange = { sessionStartTime = it },
                    label = { Text("Start Time (yyyy-MM-dd HH:mm)") },
                    placeholder = { Text("e.g., 2023-06-22 22:00") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && hasPermission && healthConnectAvailable
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = sessionEndTime,
                    onValueChange = { sessionEndTime = it },
                    label = { Text("End Time (yyyy-MM-dd HH:mm)") },
                    placeholder = { Text("e.g., 2023-06-23 06:00") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && hasPermission && healthConnectAvailable
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sleep Stages:",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                stages.forEachIndexed { index, stage ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = stage.type.toString(),
                            onValueChange = { newValue ->
                                stage.type = newValue.toIntOrNull() ?: 0
                            },
                            label = { Text("Stage Type (0-5)") },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading && hasPermission && healthConnectAvailable
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = stage.startTime,
                            onValueChange = { stage.startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading && hasPermission && healthConnectAvailable
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = stage.endTime,
                            onValueChange = { stage.endTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading && hasPermission && healthConnectAvailable
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { stages.removeAt(index) },
                            enabled = !isLoading && hasPermission && healthConnectAvailable
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove stage"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = { stages.add(StageInput(0, "", "")) },
                    enabled = !isLoading && hasPermission && healthConnectAvailable
                ) {
                    Text("Add Stage")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isLoading = true
                        result = "Adding sleep data..."
                        mainScope.launch {
                            try {
                                if (healthConnectClient == null) {
                                    isLoading = false
                                    result = "Health Connect client not available"
                                    return@launch
                                }

                                val sessionStart = parseLocalDateTime(sessionStartTime)
                                val sessionEnd = parseLocalDateTime(sessionEndTime)
                                if (sessionStart == null || sessionEnd == null) {
                                    isLoading = false
                                    result = "Invalid session times"
                                    return@launch
                                }

                                val stageRecords = stages.mapNotNull { stage ->
                                    val stageStart = parseLocalDateTime(stage.startTime)
                                    val stageEnd = parseLocalDateTime(stage.endTime)
                                    if (stageStart != null && stageEnd != null) {
                                        SleepSessionRecord.Stage(
                                            startTime = stageStart.toInstant(),
                                            endTime = stageEnd.toInstant(),
                                            stage = stage.type
                                        )
                                    } else {
                                        null
                                    }
                                }

                                val sleepSession = SleepSessionRecord(
                                    startTime = sessionStart.toInstant(),
                                    startZoneOffset = sessionStart.offset,
                                    endTime = sessionEnd.toInstant(),
                                    endZoneOffset = sessionEnd.offset,
                                    stages = stageRecords,
                                    metadata = HealthMetadata()
                                )

                                healthConnectClient.insertRecords(listOf(sleepSession))
                                isLoading = false
                                result = "Successfully added sleep data to Health Connect!"
                                sessionStartTime = ""
                                sessionEndTime = ""
                                stages.clear()
                            } catch (e: Exception) {
                                isLoading = false
                                result = "Error: ${e.message}"
                            }
                        }
                    },
                    enabled = !isLoading && hasPermission && healthConnectAvailable && sessionStartTime.isNotBlank() && sessionEndTime.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colors.onPrimary
                        )
                    } else {
                        Text("Save Sleep Data")
                    }
                }
            }
        }

        // Status display
        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
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
    }
}

// Helper function to parse local date-time strings
fun parseLocalDateTime(dateTimeStr: String): ZonedDateTime? {
    return try {
        ZonedDateTime.parse(
            dateTimeStr,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
        )
    } catch (e: Exception) {
        null
    }
}