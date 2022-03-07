package sdk.sahha.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.model.activities.RecognisedActivity
import sdk.sahha.android.model.activities.PreviousActivity
import sdk.sahha.android.model.steps.DetectedSteps
import sdk.sahha.android.model.steps.LastDetectedSteps

@Dao
interface MovementDao {
  // Detected Steps
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveDetectedSteps(detectedSteps: DetectedSteps)

  @Query("SELECT * FROM DetectedSteps")
  suspend fun getDetectedSteps(): List<DetectedSteps>

  @Query("DELETE FROM DetectedSteps")
  suspend fun clearDetectedSteps()

  // Last Detected Steps
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveLastDetectedSteps(lastDetectedSteps: LastDetectedSteps)

  @Query("SELECT * FROM LastDetectedSteps WHERE id=1")
  suspend fun getLastDetectedSteps(): LastDetectedSteps

  // Activity Recognition
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveDetectedActivity(recognisedActivity: RecognisedActivity)

  @Query("SELECT * FROM RecognisedActivity")
  suspend fun getRecognisedActivities(): List<RecognisedActivity>

  @Query("DELETE FROM RecognisedActivity")
  suspend fun clearActivities()

  @Query("UPDATE RecognisedActivity SET movementType=:activity, confidence=:confidence WHERE id=:id")
  suspend fun updateDetectedActivity(id: Int, activity: Int, confidence: Int)

  // Previous Activity
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun savePreviousActivity(previousActivity: PreviousActivity)

  @Query("SELECT * FROM PreviousActivity WHERE id = 1")
  suspend fun getPreviousActivity(): PreviousActivity
}
