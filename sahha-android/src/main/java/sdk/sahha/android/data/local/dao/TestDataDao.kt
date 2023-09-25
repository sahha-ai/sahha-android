package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.domain.model.test.TestDataLocal
import sdk.sahha.android.domain.model.test.TestDataPost

@Dao
interface TestDataDao {
    // Local
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTestLocalData(data: TestDataLocal)

    @Query("SELECT * FROM TestDataLocal")
    suspend fun getTestLocalData(): List<TestDataLocal>

    @Query("SELECT * FROM TestDataLocal WHERE dataType = :dataType")
    suspend fun getTestLocalDataByType(dataType: String): List<TestDataLocal>

    @Query("DELETE FROM TestDataLocal")
    suspend fun clearAllTestDataLocal()

    @Delete
    suspend fun clearTestDataLocal(data: TestDataLocal)

    // Post
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTestPostData(data: TestDataPost)

    @Query("SELECT * FROM TestDataPost")
    suspend fun getTestPostData(): List<TestDataPost>

    @Query("SELECT * FROM TestDataPost WHERE dataType = :dataType")
    suspend fun getTestPostDataByType(dataType: String): List<TestDataPost>

    @Query("DELETE FROM TestDataPost")
    suspend fun clearAllTestDataPost()

    @Delete
    suspend fun clearTestDataPost(data: TestDataPost)
}