package sdk.sahha.android.controller.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import sdk.sahha.android.data.AppDatabase
import sdk.sahha.android.data.dao.MovementDao
import sdk.sahha.android.model.activities.PreviousActivity
import sdk.sahha.android.model.activities.RecognisedActivity
import sdk.sahha.android.controller.utils.TimeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class ActivityRecognitionReceiver : BroadcastReceiver() {
  private val tag by lazy { "ActivityRecognitionReceiver" }
  private val timeController by lazy { TimeController() }
  private val defaultScope by lazy { CoroutineScope(Dispatchers.Default) }

  private lateinit var movementDao: MovementDao

  @RequiresApi(Build.VERSION_CODES.O)
  override fun onReceive(context: Context, intent: Intent) {
    Log.w(tag, "onReceive")
    defaultScope.launch {
      movementDao = AppDatabase(context).database.movementDao()

      Log.w(tag, "defaultScope")

      val trackedActivities = hashMapOf<Int, Int>()

      Log.w(
        tag,
        "ActivityRecognitionResult.hasResult: ${ActivityRecognitionResult.hasResult(intent)}"
      )

      if (ActivityRecognitionResult.hasResult(intent)) {
        val result = ActivityRecognitionResult.extractResult(intent)
        Toast.makeText(context, "hasResult: $result", Toast.LENGTH_LONG).show()
        Log.d(tag, "ActivityRecognitionResult.hasResult: $result")

        // Narrow down probable activities to what we track at Sahha
        result?.let { event ->
          val activities = arrayOf(
            event.probableActivities.find { it.type == DetectedActivity.STILL },
            event.probableActivities.find { it.type == DetectedActivity.WALKING },
            event.probableActivities.find { it.type == DetectedActivity.RUNNING },
            event.probableActivities.find { it.type == DetectedActivity.ON_BICYCLE },
            event.probableActivities.find { it.type == DetectedActivity.IN_VEHICLE }
          )

          // Add found probable activities to empty hashmap
          for (i in activities.indices) {
            if (activities[i] != null) {
              trackedActivities.put(activities[i]!!.confidence, i)
              Log.w(tag, "\nActivities $i:\n" + activities[i])
            }
          }
          Log.w(tag, "\nTracked activities hashmap:\n$trackedActivities")

          // Sort by key which is the confidence, the last element should be the highest confidence activity
          if (trackedActivities.isEmpty()) return@launch

          val mostProbable = trackedActivities.toSortedMap()
          val mostProbableActivity = mostProbable.values.last()
          val mostProbableConfidence = mostProbable.keys.last()

          // To avoid a weird entry I was receiving (all states were confidence 10)
          if (mostProbableConfidence <= 10) return@launch

          checkAndSaveLastDetectedActivity(mostProbableActivity, mostProbableConfidence)


          Log.e(
            tag,
            "\nActivity: $mostProbableActivity\nConfidence: $mostProbableConfidence\nTime: ${timeController.nowInISO()}"
          )
        }
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private suspend fun checkAndSaveLastDetectedActivity(
    mostProbableActivity: Int,
    mostProbableConfidence: Int,
  ) {
    if (movementDao.getPreviousActivity() == null) {
      Log.d(tag, "getLastDetectedActivity is null")
      saveRecognisedActivity(mostProbableActivity, mostProbableConfidence)
      Log.w(tag, "Activity saved: ${movementDao.getRecognisedActivities()}")
      saveLastDetectedActivity(mostProbableActivity, mostProbableConfidence)
      Log.w(tag, "Previous activity saved: ${movementDao.getPreviousActivity()}")
      return
    }

    val lastActivity = movementDao.getPreviousActivity()
    Log.d(tag, "Checking last activity: ${movementDao.getPreviousActivity()}")

    if (lastActivity.activity != mostProbableActivity) {
      Log.d(tag, "${movementDao.getPreviousActivity()} != $mostProbableActivity")
      saveRecognisedActivity(mostProbableActivity, mostProbableConfidence)
      Log.d(tag, "Saved: $mostProbableActivity, $mostProbableConfidence")
      saveLastDetectedActivity(mostProbableActivity, mostProbableConfidence)
      Log.d(tag, "Replaced last activity with: $mostProbableActivity")
      return
    }

    // The detected activity is the same as last activity
    // Check and save if higher confidence
    if (movementDao.getRecognisedActivities().isEmpty()) {
      return
    }

    if (lastActivity.confidence < mostProbableConfidence) {
      val mostRecentId = movementDao.getRecognisedActivities().last().id
      movementDao.updateDetectedActivity(
        mostRecentId,
        mostProbableActivity,
        mostProbableConfidence
      )
      movementDao.savePreviousActivity(
        PreviousActivity(
          mostProbableActivity,
          mostProbableConfidence
        )
      )
    }

  }

  @RequiresApi(Build.VERSION_CODES.O)
  private suspend fun saveRecognisedActivity(
    mostProbableActivity: Int,
    mostProbableConfidence: Int,
  ) {
    val nowInISO = timeController.nowInISO()
    Log.e(tag, "saveDetectedActivity nowInISO: $nowInISO")
    movementDao.saveDetectedActivity(
      RecognisedActivity(
        mostProbableActivity,
        mostProbableConfidence,
        nowInISO
      )
    )
  }

  private suspend fun saveLastDetectedActivity(
    mostProbableActivity: Int,
    mostProbableConfidence: Int,
  ) {
    movementDao.savePreviousActivity(PreviousActivity(mostProbableActivity, mostProbableConfidence))
  }
}
