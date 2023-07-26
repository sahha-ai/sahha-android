package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.activities.PreviousActivity
import sdk.sahha.android.domain.model.activities.RecognisedActivity
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession

@Dao
interface MovementDao {
    // Steps
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStepData(stepData: StepData)

    @Query("SELECT * FROM StepData")
    suspend fun getAllStepData(): List<StepData>

    @Query("SELECT count FROM StepData WHERE id=(SELECT MAX(id) FROM StepData WHERE source='AndroidStepCounter')")
    suspend fun getLastStepCount(): Int?

    @Query("SELECT count FROM StepData WHERE source='AndroidStepCounter' AND count=:count")
    suspend fun getExistingStepCount(count: Int): Int?

    @Query("SELECT * FROM StepData WHERE source=:source")
    suspend fun getSourceStepData(source: String): List<StepData>

    @Query("DELETE FROM StepData WHERE source=:source")
    suspend fun clearSourceStepData(source: String)

    @Query("DELETE FROM StepData")
    suspend fun clearAllStepData()

    @Query("DELETE FROM StepData WHERE id IN (SELECT id FROM StepData ORDER BY id DESC LIMIT :amount)")
    suspend fun clearFirstStepData(amount: Int)

    @Delete
    suspend fun clearStepData(stepData: List<StepData>)

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

    // Step Session

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStepSession(stepSession: StepSession)
    @Query("SELECT * FROM StepSession")
    suspend fun getAllStepSessions(): List<StepSession>
    @Delete
    suspend fun clearStepSessions(stepSessions: List<StepSession>)
    @Query("DELETE FROM StepSession")
    suspend fun clearAllStepSessions()
}
