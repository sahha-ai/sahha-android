package sdk.sahha.android.domain.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha
import sdk.sahha.android.domain.model.activities.PreviousActivity
import sdk.sahha.android.domain.model.activities.RecognisedActivity
import java.util.*

@RequiresApi(Build.VERSION_CODES.N)
class ActivityRecognitionReceiver : BroadcastReceiver() {
    private val tag by lazy { "ActivityRecognitionReceiver" }

    private lateinit var activities: Array<DetectedActivity?>
    private lateinit var trackedActivities: HashMap<Int, Int>
    private lateinit var mostProbable: SortedMap<Int, Int>
    private var mostProbableActivity: Int = 0
    private var mostProbableConfidence: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Sahha.di.defaultScope.launch {
            checkActivities(intent)
        }
    }

    private suspend fun checkActivities(intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            filterActivities(result)
        }
    }

    private suspend fun filterActivities(result: ActivityRecognitionResult?) {
        // Narrow down probable activities to what we track at Sahha
        result?.let { event ->
            activities = getActivities(event)

            populateTrackedActivities()

            if (trackedActivities.isEmpty()) return@let

            setMostProbableData()

            // To avoid a weird entry I was receiving (all states were confidence 10)
            if (mostProbableConfidence <= 10) return@let

            checkAndSaveLastDetectedActivityAsync(mostProbableActivity, mostProbableConfidence)
        }
    }

    private fun setMostProbableData() {
        // Sort by key which is the confidence, the last element should be the highest confidence activity
        mostProbable = trackedActivities.toSortedMap()
        mostProbableActivity = mostProbable.values.last()
        mostProbableConfidence = mostProbable.keys.last()
    }

    private fun populateTrackedActivities() {
        // Add found probable activities to empty hashmap
        for (i in activities.indices) {
            if (activities[i] != null) {
                trackedActivities.put(activities[i]!!.confidence, i)
            }
        }
    }

    private fun getActivities(event: ActivityRecognitionResult): Array<DetectedActivity?> {
        return arrayOf(
            event.probableActivities.find { it.type == DetectedActivity.STILL },
            event.probableActivities.find { it.type == DetectedActivity.WALKING },
            event.probableActivities.find { it.type == DetectedActivity.RUNNING },
            event.probableActivities.find { it.type == DetectedActivity.ON_BICYCLE },
            event.probableActivities.find { it.type == DetectedActivity.IN_VEHICLE }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun checkAndSaveLastDetectedActivityAsync(
        mostProbableActivity: Int,
        mostProbableConfidence: Int,
    ) {
        val previousActivityIsEmpty = Sahha.di.movementDao.getPreviousActivity() == null
        val recognisedActivitiesAreEmpty = Sahha.di.movementDao.getRecognisedActivities().isEmpty()

        if (previousActivityIsEmpty) {
            saveRecognisedActivity(mostProbableActivity, mostProbableConfidence)
            saveLastDetectedActivity(mostProbableActivity, mostProbableConfidence)
            return
        }

        val lastActivity = Sahha.di.movementDao.getPreviousActivity()

        if (lastActivity.activity != mostProbableActivity) {
            saveRecognisedActivity(mostProbableActivity, mostProbableConfidence)
            saveLastDetectedActivity(mostProbableActivity, mostProbableConfidence)
            return
        }

        // The detected activity is the same as last activity
        // Check and save if higher confidence
        if (recognisedActivitiesAreEmpty) return
        if (lastActivity.confidence < mostProbableConfidence) updateDetectedAndPreviousActivity()
    }

    private suspend fun updateDetectedAndPreviousActivity() {
        val mostRecentId = Sahha.di.movementDao.getRecognisedActivities().last().id
        Sahha.di.movementDao.updateDetectedActivity(
            mostRecentId,
            mostProbableActivity,
            mostProbableConfidence
        )
        Sahha.di.movementDao.savePreviousActivity(
            PreviousActivity(
                mostProbableActivity,
                mostProbableConfidence
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun saveRecognisedActivity(
        mostProbableActivity: Int,
        mostProbableConfidence: Int,
    ) {
        val nowInISO = Sahha.timeManager.nowInISO()
        Log.e(tag, "saveDetectedActivity nowInISO: $nowInISO")
        Sahha.di.movementDao.saveDetectedActivity(
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
        Sahha.di.movementDao.savePreviousActivity(
            PreviousActivity(
                mostProbableActivity,
                mostProbableConfidence
            )
        )
    }
}