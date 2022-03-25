package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sahha.android.model.SleepQueue
import com.sahha.android.model.SleepQueueHistory

@Dao
interface SleepDao {
    // Sleep queue
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun saveSleep(sleep: SleepQueue)

    @Query("SELECT * FROM SleepQueue WHERE startMillis=:startMillis AND endMillis=:endMillis")
    suspend fun getSleepWith(startMillis: Long, endMillis: Long): List<SleepQueue>

    @Query("SELECT * FROM SleepQueue")
    suspend fun getSleepQueue(): List<SleepQueue>

    @Query("DELETE FROM SleepQueue WHERE id=:id")
    suspend fun removeSleep(id: Int)

    @Query("SELECT * FROM SleepQueue WHERE createdAt=:createdAt")
    suspend fun getSleepQueueCreatedAt(createdAt: String): SleepQueue

    // Sleep queue history
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSleepHistory(sleep: SleepQueueHistory)

    @Query("SELECT * FROM SleepQueueHistory WHERE startMillis=:startMillis AND endMillis=:endMillis")
    suspend fun getSleepHistoryWith(startMillis: Long, endMillis: Long): List<SleepQueueHistory>

    @Query("SELECT * FROM SleepQueueHistory")
    suspend fun getSleepQueueHistory(): List<SleepQueueHistory>

    @Query("DELETE FROM SleepQueueHistory WHERE id=:id")
    suspend fun removeHistory(id: Int)
}