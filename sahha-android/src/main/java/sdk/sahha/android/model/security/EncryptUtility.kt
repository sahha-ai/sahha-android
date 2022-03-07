package sdk.sahha.android.model.security

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EncryptUtility(
    @PrimaryKey val alias: String,
    val iv: ByteArray,
    val encryptedData: ByteArray
)
