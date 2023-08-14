package empty.sahha.android

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.health.connect.datatypes.StepsRecord
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import empty.sahha.android.ui.theme.SahhasdkemptyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.R
import sdk.sahha.android.activity.health_connect.SahhaHealthConnectPermissionActivity
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaNotificationConfiguration
import sdk.sahha.android.source.SahhaSettings
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Date

const val SEVEN_DAYS_MILLIS = 604800000L

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = SahhaSettings(
            environment = SahhaEnvironment.development,
            notificationSettings = SahhaNotificationConfiguration(
                icon = R.drawable.ic_test,
                title = "Test",
                shortDescription = "This is a test."
            )
        )

        Sahha.configure(
            application,
            config,
        ) { error, success ->
            lifecycleScope.launch {
                Toast.makeText(this@MainActivity, error ?: "Successful $success", Toast.LENGTH_LONG)
                    .show()
            }
        }

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
                    var appId by remember { mutableStateOf("") }
                    var appSecret by remember { mutableStateOf("") }
                    var externalId by remember { mutableStateOf("") }
                    var start by remember { mutableStateOf("") }
                    var authStatus by remember { mutableStateOf("Pending") }

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
                                Greeting(greeting)
                                Spacer(modifier = Modifier.padding(16.dp))
                                HealthConnectPermission(context = this@MainActivity)
                                ForegroundQuery(context = this@MainActivity)
                                AggregateSteps()
                                AggregateSleepSessions()
                                Text(permissionStatus)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.enableSensors(
                                        this@MainActivity,
                                    ) { error, status ->
                                        permissionStatus = status.name
                                    }
                                }) {
                                    Text("Permission Test")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.configure(
                                        application,
                                        config
                                    ) { error, success ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            error ?: "Successful $success",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }) {
                                    Text("Configure")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    lifecycleScope.launch {
                                        SahhaReconfigure(application)
                                    }
                                }) {
                                    Text("Reconfigure")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                OutlinedTextField(
                                    value = appId,
                                    singleLine = true,
                                    onValueChange = {
                                        appId = it
                                    }, label = {
                                        Text("App ID")
                                    })
                                OutlinedTextField(
                                    value = appSecret,
                                    singleLine = true,
                                    onValueChange = {
                                        appSecret = it
                                    }, label = {
                                        Text("App Secret")
                                    })
                                OutlinedTextField(
                                    value = externalId,
                                    singleLine = true,
                                    onValueChange = {
                                        externalId = it
                                    }, label = {
                                        Text("External ID")
                                    })
                                Button(onClick = {
                                    authStatus = "Loading..."
                                    Sahha.authenticate(
                                        appId,
                                        appSecret,
                                        externalId
                                    ) { error, success ->
                                        if (success) authStatus = "Successful" else authStatus =
                                            error ?: "Failed"
                                    }
                                }) {
                                    Text("Authenticate")
                                }
                                Text(authStatus)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.openAppSettings(this@MainActivity)
                                }) {
                                    Text("Open Settings")
                                }
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {

                                }) {
                                    Text("Test start")
                                }
                                Text(start)
                                Spacer(modifier = Modifier.padding(16.dp))
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
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    analyzeResponse = ""

                                    val now = Date()
                                    val lastWeek = Date(now.time - SEVEN_DAYS_MILLIS)

                                    Sahha.analyze { error, success ->
                                        error?.also { analyzeResponse = it }
                                        success?.also {
                                            analyzeResponse = it
                                        }
                                    }

                                    Sahha.analyze(
                                        dates = Pair(lastWeek, now),
                                    ) { error, success ->
                                        error?.also { analyzeResponseDate = it }
                                        success?.also {
                                            analyzeResponseDate = it
                                        }
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        Sahha.analyze(
                                            dates = Pair(LocalDateTime.now(), LocalDateTime.now()),
                                        ) { error, success ->
                                            error?.also { analyzeResponseLocalDateTime = it }
                                            success?.also {
                                                analyzeResponseLocalDateTime = it
                                            }
                                        }
                                    } else {
                                        analyzeResponseLocalDateTime = "Version too low"
                                    }
                                }) {
                                    Text("Analyze")
                                }
                                Text(analyzeResponse)
                                Text(analyzeResponseDate)
                                Text(analyzeResponseLocalDateTime)
                                Spacer(modifier = Modifier.padding(16.dp))
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
                                Spacer(modifier = Modifier.padding(16.dp))
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
                                Spacer(modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
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

private lateinit var notification: Notification

@Composable
fun ForegroundQuery(context: Context) {
    // For HealthConnect
    val name = "My Service Channel"
    val descriptionText = "Channel for foreground service"
    val importance = NotificationManager.IMPORTANCE_MIN
    val channel = NotificationChannel("tester_channel", name, importance).apply {
        description = descriptionText
    }
    val notificationManager: NotificationManager =
        context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)

    Text("Query via ForegroundService")
    Spacer(modifier = Modifier.padding(16.dp))
    Button(onClick = {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val timeInMillis = Instant.now().plus(10, ChronoUnit.SECONDS).toEpochMilli()

        val alarmIntent = Intent(context, MyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        Toast.makeText(context, "Alarm set at ${Instant.ofEpochMilli(timeInMillis)}", Toast.LENGTH_LONG).show()

        // Run foreground directly
//        val serviceIntent = Intent(context, TesterService::class.java)
//        context.startForegroundService(serviceIntent)
    }) {
        Text("Query Test")
    }
    Spacer(modifier = Modifier.padding(16.dp))
}

@Composable
fun HealthConnectPermission(context: Context) {
    // TODO Fix invis barrier
    Text("HealthConnect Permission")
    Spacer(modifier = Modifier.padding(16.dp))
    Button(onClick = {
        val intent = Intent(context, SahhaHealthConnectPermissionActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }) {
        Text("Grant")
    }
    Spacer(modifier = Modifier.padding(16.dp))
}

@Composable
fun AggregateSteps() {
    var data by remember { mutableStateOf("Step data") }

    Text("Aggregate Step Data")
    Spacer(modifier = Modifier.padding(16.dp))
    Button(onClick = {
        data = "Loading..."
        Sahha.getAggregateSteps { err, steps ->
            data = ""
            steps?.forEach {
                data += "$it\n\n"
            }
        }
    }) {
        Text("Get Steps")
    }
    Spacer(modifier = Modifier.padding(16.dp))
    Text(data)
    Spacer(modifier = Modifier.padding(16.dp))
}

@Composable
fun AggregateSleepSessions() {
    var data by remember { mutableStateOf("Sleep Session data") }

    Text("Aggregate Sleep Session Data")
    Spacer(modifier = Modifier.padding(16.dp))
    Button(onClick = {
        data = "Loading..."
        Sahha.getAggregateSleepSessions { err, sleepSessions ->
            data = ""
            sleepSessions?.forEach {
                data += "$it\n\n"
            }
        }
    }) {
        Text("Get Sleep Sessions")
    }
    Spacer(modifier = Modifier.padding(16.dp))
    Text(data)
    Spacer(modifier = Modifier.padding(16.dp))
}

class TesterService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("Creating notification")
        val notification: Notification = NotificationCompat.Builder(this, "tester_channel")
            .setContentTitle("Analytics")
            .setContentText("Data is being sent...")
            .setSmallIcon(R.drawable.ic_test)
            .build()

        println("Notification created")
        startForeground(12341234, notification)
        println("Starting foreground")

        CoroutineScope(Dispatchers.IO).launch {
            SahhaReconfigure(this@TesterService)
            println("Sahha reconfigured")
            val result = Sahha.ableToReadSteps()
            println("Result complete")

            println("Able to read steps: $result")

            delay(5000)
            stopSelf()
        }

        return START_NOT_STICKY
    }
}

class MyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, TesterService::class.java)
        context.startForegroundService(serviceIntent)
    }
}