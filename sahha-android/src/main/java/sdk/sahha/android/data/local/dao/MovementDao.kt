package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.domain.model.activities.PreviousActivity
import sdk.sahha.android.domain.model.activities.RecognisedActivity
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.domain.model.steps.StepsHealthConnect

@Dao
internal interface MovementDao {
    // Steps
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStepData(stepData: StepData)

    @Query("SELECT * FROM StepData")
    suspend fun getAllStepData(): List<StepData>

    @Query("SELECT count FROM StepData WHERE id=(SELECT MAX(id) FROM StepData WHERE source='AndroidStepCounter')")
    suspend fun getLastStepCount(): Int?

    @Query("SELECT count FROM StepData WHERE source='AndroidStepCounter' AND count=:count")
    suspend fun getExistingStepCount(count: Int): Int?

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

    // Steps Health Connect
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStepsHc(stepsHc: StepsHealthConnect)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStepsListHc(stepsListHc: List<StepsHealthConnect>)

    @Query("SELECT * FROM StepsHealthConnect")
    suspend fun getAllStepsHc(): List<StepsHealthConnect>

    @Delete
    suspend fun clearStepsListHc(stepsHc: List<StepsHealthConnect>)

    @Query("DELETE FROM StepsHealthConnect")
    suspend fun clearAllStepsHc()

    @Query("DELETE FROM StepsHealthConnect WHERE endDateTime < :dateTimeIso")
    suspend fun clearStepsBeforeHc(dateTimeIso: String)
}
