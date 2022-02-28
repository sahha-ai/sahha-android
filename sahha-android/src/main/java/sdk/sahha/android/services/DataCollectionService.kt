package sdk.sahha.android.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import sdk.sahha.android.data.AVG_STEP_DISTANCE_MALE
import sdk.sahha.android.data.AppDatabase
import sdk.sahha.android.data.MovementDao
import sdk.sahha.android.data.NOTIFICATION_DATA_COLLECTION
import sdk.sahha.android.model.steps.DetectedSteps
import sdk.sahha.android.model.steps.LastDetectedSteps
import sdk.sahha.android.utils.NotificationController
import sdk.sahha.android.utils.TimeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class DataCollectionService : Service() {
  private val tag by lazy { "DataCollectionService" }

  private val defaultScope by lazy { CoroutineScope(Default) }
  private val ioScope by lazy { CoroutineScope(IO) }
  private val movementDao by lazy {
    AppDatabase(this).database.movementDao()
  }
  private val tc by lazy { TimeController() }
  private var pedometerRegistered = false

  override fun onBind(intent: Intent?): IBinder? {
    Log.w(tag, "onBind")
    return null
  }

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.w(tag, "onStartCommand")

    val notification = NotificationController().getNewNotification(
      this,
      "phoneUsage",
      "Phone Usage",
      NotificationManager.IMPORTANCE_MIN,
      "Analytics are running",
      "Swipe for options to hide this notification.",
      true
    )
    startForeground(NOTIFICATION_DATA_COLLECTION, notification)

    runPedometer(
      (applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager),
      movementDao
    )

    Toast.makeText(this, "onStartCommand", Toast.LENGTH_LONG).show()

    return START_STICKY
  }

  private fun runPedometer(sensorManager: SensorManager, movementDao: MovementDao) {
    Log.w(tag, "runPedometer")
    defaultScope.launch {
      Log.w(tag, "defaultScope")

      // Get user's gender
      var steps = 0f
      var lastSteps = 0
      var distance: Double = 0.0
      var elapsedTimeInMillis = 0L
      var startOfSteps = 0L
      var startDateTime = ""
      var endDateTime = ""
      var createdAt = ""

      val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
      val sensorListener2 = object : SensorEventListener2 {
        override fun onSensorChanged(sensorEvent: SensorEvent?) {
          Log.w(tag, "onSensorChanged")
          ioScope.launch {
            Log.w(tag, "ioScope")
            sensorEvent?.let { event ->
              Log.w(tag, "sensorEvent")
              val distancePerStepMetres = AVG_STEP_DISTANCE_MALE

              val totalSteps = event.values[0]

              // Check if last steps null
              val lastDetectedSteps = movementDao.getLastDetectedSteps() ?: null

              fun setDataFromTotalSteps() {
                // Add steps to both last and current detected steps
                steps = event.values[0]
                // Distance of these steps
                distance = steps * distancePerStepMetres
                // Set start time to be start of device boot
                elapsedTimeInMillis = tc.convertNanosToMillis(event.timestamp)
                startOfSteps = tc.nowInEpoch() - elapsedTimeInMillis
                startDateTime = tc.epochMillisToISO(startOfSteps)
                // end time to manual time stamp
                endDateTime = tc.nowInISO()
                // created at to manual time stamp
                createdAt = tc.nowInISO()
              }

              fun setDataFromLastSteps() {
                lastSteps = lastDetectedSteps!!.steps
                // current steps minus last steps
                steps = event.values[0] - lastSteps
                // distance of steps
                distance = steps * distancePerStepMetres
                // start time is end of last steps end time
                startDateTime = lastDetectedSteps.endDateTime
                // end time to manual time stamp
                endDateTime = tc.nowInISO()
                // created at to manual time stamp
                createdAt = tc.nowInISO()

                Log.w(tag, "event.timestamp: ${event.timestamp}")

                Log.w(
                  tag,
                  "startDateTime: $startDateTime"
                )
                Log.w(
                  tag,
                  "endDateTime: $endDateTime"
                )
              }

              if (lastDetectedSteps == null) {
                setDataFromTotalSteps()
              } else if (totalSteps < lastDetectedSteps.steps) {
                setDataFromTotalSteps()
              } else {
                setDataFromLastSteps()
              }

              // Last steps must always be the total so we can get the correct differences
              val lastStepsInt = event.values[0].toInt()
              val stepsInt = steps.toInt()
              val distanceInt = distance.toInt()

              movementDao.saveDetectedSteps(
                DetectedSteps(
                  stepsInt,
                  distanceInt,
                  startDateTime,
                  endDateTime,
                  createdAt
                )
              )
              movementDao.saveLastDetectedSteps(
                LastDetectedSteps(
                  lastStepsInt,
                  distanceInt,
                  startDateTime,
                  endDateTime,
                  createdAt
                )
              )
            }
          }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onFlushCompleted(sensor: Sensor?) {}
      }

      sensor?.let { _sensor ->
        if (pedometerRegistered) return@launch

        sensorManager.registerListener(
          sensorListener2,
          _sensor,
          SensorManager.SENSOR_DELAY_NORMAL
        )
        pedometerRegistered = true
      }
    }
  }
}
