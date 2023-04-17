package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.domain.model.security.EncryptUtility

@Dao
@Deprecated("Now using EncryptedSharedPreferences instead. This is only used for migration purposes.")
interface SecurityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEncryptUtility(encryptUtility: EncryptUtility)

    @Query("SELECT alias, iv, encryptedData FROM EncryptUtility WHERE alias=:alias")
    suspend fun getEncryptUtility(alias: String): EncryptUtility

    @Query("DELETE FROM EncryptUtility")
    suspend fun deleteAllEncryptedData()
}