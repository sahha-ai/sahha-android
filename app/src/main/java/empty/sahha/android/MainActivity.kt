package empty.sahha.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import empty.sahha.android.ui.theme.SahhasdkemptyTheme
import kotlinx.coroutines.launch
import sdk.sahha.android.R
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.common.enums.HealthConnectSensor
import sdk.sahha.android.source.*
import java.time.LocalDateTime
import java.util.*

private const val SEVEN_DAYS_MILLIS = 604800000L

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = SahhaSettings(
            environment = SahhaEnvironment.development,
            notificationSettings = SahhaNotificationConfiguration(
                icon = empty.sahha.android.R.drawable.ic_launcher_foreground,
                title = "Sahha SDK",
                shortDescription = "Sahha SDK foreground service"
            ),
            postSensorDataManually = true,
        )
        Sahha.configure(
            application,
            config,
        )

        setContent {
            SahhasdkemptyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var greeting by remember { mutableStateOf("Android") }
                    var permissionStatus by remember { mutableStateOf("") }
                    var manualPost by remember { mutableStateOf("") }
                    var manualPostDevice by remember { mutableStateOf("") }
                    var analyzeResponse by remember { mutableStateOf("") }
                    var analyzeResponseEpoch by remember { mutableStateOf("") }
                    var analyzeResponseDate by remember { mutableStateOf("") }
                    var analyzeResponseLocalDateTime by remember { mutableStateOf("") }
                    var postDemo by remember { mutableStateOf("") }
                    var getDemo by remember { mutableStateOf("") }
                    var token by remember { mutableStateOf("") }
                    var refreshToken by remember { mutableStateOf("") }
                    var start by remember { mutableStateOf("") }
                    var healthConnectStatus by remember { mutableStateOf("Pending") }
                    var healthConnectData by remember { mutableStateOf("") }
                    var healthConnectPostStatus by remember { mutableStateOf("Pending") }

                    var hcSelectedSensor: HealthConnectSensor? by remember { mutableStateOf(null) }
                    var hcSensorExpanded by remember { mutableStateOf(false) }

                    Sahha.getSensorStatus(
                        this@MainActivity
                    ) { error, sensorStatus ->
                        permissionStatus = sensorStatus.name
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                MySpacer()
                                Text(healthConnectStatus)
                                MySpacer()
                                Button(onClick = {
                                    Sahha.enableHealthConnect(
                                        this@MainActivity,
                                    ) { error, status ->
                                        healthConnectStatus =
                                            error ?: status.name
                                    }
                                }) {
                                    Text("HealthConnect Permission")
                                }

                                MySpacer()
                                Text(healthConnectPostStatus)
                                MySpacer()
                                Button(onClick = {
                                    healthConnectPostStatus = "Loading..."
                                    Sahha.postHealthConnectData { error, _ ->
                                        healthConnectPostStatus = error ?: "Successful"
                                    }
                                }) {
                                    Text("HealthConnect Post")
                                }
                                MySpacer()

                                DropDownWrapper(options = HealthConnectSensor.values()) {
                                    hcSelectedSensor = it
                                }
                                Button(onClick = {
                                    lifecycleScope.launch {
                                        healthConnectData = ""
                                        healthConnectData = "Loading..."
                                        hcSelectedSensor?.also { sensor ->
                                            Sahha.getHealthConnectData(
                                                sensor,
                                            ) { error, status ->
                                                healthConnectData = error ?: status ?: "error"
                                            }
                                        }
                                    }
                                }) {
                                    Text("HealthConnect Read")
                                }
                                Text(healthConnectData)
                                MySpacer()

                                Greeting(greeting)
                                MySpacer()
                                Text(permissionStatus)
                                MySpacer()
                                Button(onClick = {
                                    Sahha.enableSensors(
                                        this@MainActivity,
                                    ) { error, status ->
                                        permissionStatus = status.name
                                    }
                                }) {
                                    Text("Permission Test")
                                }
                                MySpacer()
                                Button(onClick = {
                                    Sahha.configure(
                                        application,
                                        SahhaSettings(SahhaEnvironment.development)
                                    )
                                }) {
                                    Text("Configure")
                                }
                                MySpacer()
                                Button(onClick = {
                                    lifecycleScope.launch {
                                        SahhaReconfigure(application)
                                    }
                                }) {
                                    Text("Reconfigure")
                                }
                                MySpacer()
                                OutlinedTextField(
                                    value = token,
                                    singleLine = true,
                                    onValueChange = {
                                        token = it
                                    }, label = {
                                        Text("Token")
                                    })
                                OutlinedTextField(
                                    value = refreshToken,
                                    singleLine = true,
                                    onValueChange = {
                                        refreshToken = it
                                    }, label = {
                                        Text("Refresh Token")
                                    })
                                Button(onClick = {
                                    Sahha.authenticate(
                                        token,
                                        refreshToken
                                    ) { error, success ->
                                        if (success) greeting = "Successful" else greeting =
                                            error ?: "Failed"
                                    }
                                }) {
                                    Text("Authenticate")
                                }
                                MySpacer()
                                Button(onClick = {
                                    Sahha.openAppSettings(this@MainActivity)
                                }) {
                                    Text("Open Settings")
                                }
                                MySpacer()
                                Button(onClick = {

                                }) {
                                    Text("Test start")
                                }
                                Text(start)
                                MySpacer()
                                Button(onClick = {
                                    manualPost = ""
                                    Sahha.postSensorData { error, success ->
                                        if (success) manualPost = "Successful" else manualPost =
                                            error ?: "Failed"
                                    }
                                }) {
                                    Text("Manual Post All")
                                }
                                Text(manualPost)
                                MySpacer()
                                Button(onClick = {
                                    analyzeResponse = ""

                                    val now = Date()
                                    val lastWeek = Date(now.time - SEVEN_DAYS_MILLIS)

                                    Sahha.analyze(includeSourceData = true) { error, success ->
                                        error?.also { analyzeResponse = it }
                                        success?.also {
                                            analyzeResponse = it
                                        }
                                    }

                                    Sahha.analyze(
                                        dates = Pair(lastWeek, now),
                                        includeSourceData = true
                                    ) { error, success ->
                                        error?.also { analyzeResponseDate = it }
                                        success?.also {
                                            analyzeResponseDate = it
                                        }
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        Sahha.analyze(
                                            dates = Pair(LocalDateTime.now(), LocalDateTime.now()),
                                            includeSourceData = true
                                        ) { error, success ->
                                            error?.also { analyzeResponseLocalDateTime = it }
                                            success?.also {
                                                analyzeResponseLocalDateTime = it
                                            }
                                        }
                                    } else {
                                        analyzeResponseLocalDateTime =
                                            SahhaErrors.androidVersionTooLow(8)
                                    }
                                }) {
                                    Text("Analyze")
                                }
                                Text(analyzeResponse)
                                Text(analyzeResponseDate)
                                Text(analyzeResponseLocalDateTime)
                                MySpacer()
                                Button(onClick = {
                                    postDemo = ""
                                    Sahha.postDemographic(
                                        SahhaDemographic(
                                            31,
                                            "Male",
                                            "NZ",
                                            "KR",
                                            "South Korean",
                                            "Software Developer",
                                            "Information Technology",
                                            "$40K - $69K",
                                            "Tertiary",
                                            "Spouse",
                                            "Urban",
                                            "Renting",
                                            "1990-01-01"
                                        )
                                    ) { error, success ->
                                        if (success)
                                            postDemo = "Successful"
                                        else
                                            error?.also { postDemo = it }
                                    }
                                }) {
                                    Text("Post Demographic")
                                }
                                Text(postDemo)
                                MySpacer()
                                Button(onClick = {
                                    Sahha.getDemographic { error, demographic ->
                                        error?.also { getDemo = it }
                                        demographic?.also {
                                            getDemo =
                                                "${it.age}, ${it.gender}, ${it.country}, ${it.birthCountry}, ${it.ethnicity}, ${it.occupation}, ${it.industry}, ${it.incomeRange}, ${it.education}, ${it.relationship}, ${it.locale}, ${it.livingArrangement}, ${it.birthDate}"
                                        }
                                    }
                                }) {
                                    Text("Get Demographic")
                                }
                                Text(getDemo)
                                MySpacer()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropDownWrapper(
    options: Array<HealthConnectSensor>,
    callback: ((sensor: HealthConnectSensor?) -> Unit)
) {
    var expanded by remember { mutableStateOf(false) }
    var selected: HealthConnectSensor? by remember { mutableStateOf(null) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selected?.name ?: "Please select")
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Down arrow"
                )
            }
            DropdownMenu(
                expanded = expanded, onDismissRequest = { expanded = false },
                modifier = Modifier.wrapContentSize(),
            ) {
                for (option in options) {
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selected = option
                            callback(selected)
                            expanded = false
                        }
                    ) {
                        Text(option.name)
                    }
                }
            }
        }

    }
}

@Composable
fun MySpacer(padding: Dp = 16.dp) {
    Spacer(modifier = Modifier.padding(padding))
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SahhasdkemptyTheme {
        Greeting("Android")
    }
}