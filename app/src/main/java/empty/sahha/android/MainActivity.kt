package empty.sahha.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import empty.sahha.android.ui.theme.SahhasdkemptyTheme
import empty.sahha.android.views.*
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaNotificationConfiguration
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings

private const val MY_SHARED_PREFS = "my_shared_prefs"

// Navigation routes
object Routes {
    const val HOME = "home"
    const val SAMPLES = "samples"
    const val STATS = "stats"
    const val BIOMARKERS = "biomarkers"
    const val PERMISSIONS = "permissions"
    const val SENSOR_STATUS = "sensor_status"
    const val CONFIGURATION = "configuration"
    const val ERROR_LOG = "error_log"
    const val AUTHENTICATION = "authentication"
    const val SETTINGS = "settings"
    const val SCORES = "scores"
    const val DEMOGRAPHIC = "demographic"
    const val FORCE_CRASH = "force_crash"
    const val ENERGY_CONSUMED = "energy_consumed";
}

// Menu item data class
data class MenuItem(
    val title: String,
    val route: String,
    val description: String
)

class MainActivity : ComponentActivity() {
    private lateinit var config: SahhaSettings
    private lateinit var sensors: Set<SahhaSensor>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences(MY_SHARED_PREFS, MODE_PRIVATE)

        config = SahhaSettings(
            environment = SahhaEnvironment.development,
            notificationSettings = SahhaNotificationConfiguration(
                icon = androidx.appcompat.R.drawable.abc_btn_check_to_on_mtrl_015,
            ),
        )

        sensors = setOf(
            SahhaSensor.device_lock,
            SahhaSensor.steps,
            SahhaSensor.sleep,
            SahhaSensor.heart_rate,
            SahhaSensor.energy_consumed,
            SahhaSensor.heart_rate_variability_sdnn
        )

        Sahha.configure(this, config)

        setContent {
            SahhasdkemptyTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    AppNavigation(
                        navController = navController,
                        activity = this,
                        config = config,
                        sensors = sensors,
                        sharedPrefs = sharedPrefs
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    activity: ComponentActivity,
    config: SahhaSettings,
    sensors: Set<SahhaSensor>,
    sharedPrefs: android.content.SharedPreferences
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(navController, activity)
        }
        composable(Routes.SAMPLES) {
            ScreenWrapper(navController, "Samples") {
                SamplesView()
            }
        }
        composable(Routes.STATS) {
            ScreenWrapper(navController, "Statistics") {
                StatsView()
            }
        }
        composable(Routes.BIOMARKERS) {
            ScreenWrapper(navController, "Biomarkers") {
                BiomarkersView()
            }
        }
        composable(Routes.PERMISSIONS) {
            ScreenWrapper(navController, "Permission Tests") {
                PermissionStateTestView()
            }
        }
        composable(Routes.SENSOR_STATUS) {
            ScreenWrapper(navController, "Sensor Status") {
                SensorStatusView(activity, sensors)
            }
        }
        composable(Routes.CONFIGURATION) {
            ScreenWrapper(navController, "Configuration") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ConfigurationView(activity, config)
                }
            }
        }
        composable(Routes.ERROR_LOG) {
            ScreenWrapper(navController, "Error Logging") {
                ErrorLogView()
            }
        }
        composable(Routes.AUTHENTICATION) {
            ScreenWrapper(navController, "Authentication") {
                AuthenticationView(sharedPrefs)
            }
        }
        composable(Routes.SETTINGS) {
            ScreenWrapper(navController, "App Settings") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Sahha.openAppSettings(activity)
                }
            }
        }
        composable(Routes.SCORES) {
            ScreenWrapper(navController, "Scores") {
                ScoresView()
            }
        }
        composable(Routes.DEMOGRAPHIC) {
            ScreenWrapper(navController, "Demographics") {
                DemographicView()
            }
        }
        composable(Routes.ENERGY_CONSUMED) {
            ScreenWrapper(navController, "Energy Consumed") {
                EnergyConsumedView(activity)
            }
        }
        composable(Routes.FORCE_CRASH) {
            ScreenWrapper(navController, "Force Crash Test") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ForceCrashTestView()
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController, activity: ComponentActivity) {
    val menuItems = listOf(
        MenuItem("Authentication", Routes.AUTHENTICATION, "Authenticate with Sahha SDK"),
        MenuItem("Samples", Routes.SAMPLES, "Get sensor sample data"),
        MenuItem("Statistics", Routes.STATS, "View sensor statistics"),
        MenuItem("Biomarkers", Routes.BIOMARKERS, "Get biomarker information"),
        MenuItem("Scores", Routes.SCORES, "Get activity and sleep scores"),
        MenuItem("Demographics", Routes.DEMOGRAPHIC, "Manage demographic data"),
        MenuItem("Sensor Status", Routes.SENSOR_STATUS, "Check and enable sensors"),
        MenuItem("Energy Consumed", Routes.ENERGY_CONSUMED, "Add Energy Consumed Data"),
        MenuItem("Permission Tests", Routes.PERMISSIONS, "Test individual permissions"),
        MenuItem("Configuration", Routes.CONFIGURATION, "Configure Sahha SDK"),
        MenuItem("Error Logging", Routes.ERROR_LOG, "Post error logs"),
        MenuItem("App Settings", Routes.SETTINGS, "Open app settings"),
        MenuItem("Force Crash", Routes.FORCE_CRASH, "Test crash reporting"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sahha SDK Demo") },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            items(menuItems) { item ->
                Button(
                    onClick = {
                        if (item.route == Routes.SETTINGS) {
                            // Handle settings directly without navigation
                            Sahha.openAppSettings(activity)
                        } else {
                            // Navigate to other screens normally
                            navController.navigate(item.route)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenWrapper(
    navController: NavHostController,
    title: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            content()
        }
    }
}