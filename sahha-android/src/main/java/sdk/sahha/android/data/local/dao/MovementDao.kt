package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.domain.model.activities.RecognisedActivity
import sdk.sahha.android.domain.model.activities.PreviousActivity
import sdk.sahha.android.data.remote.dto.StepDto
import sdk.sahha.android.domain.model.steps.StepData

@Dao
interface MovementDao {
 // Steps
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveStepData(stepData: StepData)

  @Query("SELECT * FROM StepData")
  suspend fun getAllStepData(): List<StepData>

  @Query("DELETE FROM StepData")
  suspend fun clearAllStepData()

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
