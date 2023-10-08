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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import empty.sahha.android.ui.theme.SahhasdkemptyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sdk.sahha.android.R
import sdk.sahha.android.activity.health_connect.SahhaHealthConnectPermissionActivity
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaFramework
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
            environment = SahhaEnvironment.sandbox,
            notificationSettings = SahhaNotificationConfiguration(
                icon = androidx.appcompat.R.drawable.abc_btn_check_to_on_mtrl_015,
                title = "Foreground Service",
                shortDescription = "This mainly handles the steps and screen locks"
            )
        )

        Sahha.configure(
            application,
            config,
        )

        setContent {
            SahhasdkemptyTheme {
                val lfm = LocalFocusManager.current

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    var greeting by remember { mutableStateOf("Android") }
                    var permissionStatus by remember { mutableStateOf("") }
//                    var permissionStatus = "place holder"
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
                        permissionStatus = error ?: sensorStatus.name
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Greeting(greeting)
                                Spacer(modifier = Modifier.padding(16.dp))
//                                HealthConnectPermission(context = this@MainActivity)
//                                ForegroundQuery(context = this@MainActivity)
                                Text(permissionStatus)
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.enableSensors(
                                        this@MainActivity,
                                    ) { error, status ->
                                        permissionStatus = error ?: status.name
                                    }
                                }) {
                                    Text("Grant Permissions")
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
                                ErrorLogView()
                                DeauthenticateView()
                                Spacer(modifier = Modifier.padding(16.dp))
                                Text(authStatus)
                                OutlinedTextField(
                                    value = appId,
                                    singleLine = true,
                                    onValueChange = {
                                        appId = it
                                    }, label = {
                                        Text("App ID")
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        lfm.clearFocus()
                                    })
                                )
                                OutlinedTextField(
                                    value = appSecret,
                                    singleLine = true,
                                    onValueChange = {
                                        appSecret = it
                                    }, label = {
                                        Text("App Secret")
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        lfm.clearFocus()
                                    })
                                )
                                OutlinedTextField(
                                    value = externalId,
                                    singleLine = true,
                                    onValueChange = {
                                        externalId = it
                                    }, label = {
                                        Text("External ID")
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        lfm.clearFocus()
                                    })
                                )
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
                                Spacer(modifier = Modifier.padding(16.dp))
                                Button(onClick = {
                                    Sahha.openAppSettings(this@MainActivity)
                                }) {
                                    Text("Open Settings")
                                }
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

@Composable
fun DeauthenticateView() {
    var status by remember { mutableStateOf("Pending...") }

    Spacer(modifier = Modifier.padding(16.dp))
    Text(status)
    Button(onClick = {
        status = "Loading..."

        Sahha.deauthenticate { err, success ->
            err?.also {
                status = it
                return@deauthenticate
            }

            status = "De-auth successful: $success"
        }
    }) {
        Text("De-authenticate")
    }
}

@Composable
fun ErrorLogView() {
    var status by remember { mutableStateOf("Pending...") }
    var codeMethod by remember { mutableStateOf("") }
    var codePath by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var codeBody by remember { mutableStateOf("") }

    Spacer(modifier = Modifier.padding(16.dp))
    Text(status)

    MyOutlinedTextField(
        textLabel = "Message",
        textValue = message,
        onValueChange = { newValue -> message = newValue })
    MyOutlinedTextField(
        textLabel = "Path",
        textValue = codePath,
        onValueChange = { newValue -> codePath = newValue })
    MyOutlinedTextField(
        textLabel = "Method",
        textValue = codeMethod,
        onValueChange = { newValue -> codeMethod = newValue })
    MyOutlinedTextField(
        textLabel = "Body?",
        textValue = codeBody,
        onValueChange = { newValue -> codeBody = newValue })

    Button(onClick = {
        status = "Loading..."

        Sahha.postError(
            SahhaFramework.android_kotlin,
            message,
            codePath,
            codeMethod,
            codeBody
        ) { err, success ->
            err?.also {
                status = it
                return@postError
            }

            status = "Error post successful: $success"
        }
    }) {
        Text("Post Error")
    }
}

@Composable
fun MyOutlinedTextField(
    textLabel: String,
    textValue: String,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    lfm: FocusManager = LocalFocusManager.current,
    onValueChange: (newValue: String) -> Unit
) {

    OutlinedTextField(
        value = textValue,
        singleLine = singleLine,
        onValueChange = {
            onValueChange(it)
        }, label = {
            Text(textLabel)
        },
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = {
            lfm.clearFocus()
        })
    )
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
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        Toast.makeText(
            context,
            "Alarm set at ${Instant.ofEpochMilli(timeInMillis)}",
            Toast.LENGTH_LONG
        ).show()

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
            try {
//                val result = Sahha.ableToReadSteps()
//                println("Result complete")
//                println("Able to read steps: $result")
            } catch (e: Exception) {
                println(e.stackTraceToString())
            }


            delay(5000)
            stopSelf()
        }

        return START_NOT_STICKY
    }
}

class MyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val serviceIntent = Intent(context, TesterService::class.java)
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            println(e.stackTraceToString())
        }
    }
}