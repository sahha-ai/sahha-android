package sdk.sahha.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.model.security.EncryptUtility

@Dao
interface SecurityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEncryptUtility(encryptUtility: EncryptUtility)

    @Query("SELECT * FROM EncryptUtility WHERE alias=:alias")
    suspend fun getEncryptUtility(alias: String): EncryptUtility
}