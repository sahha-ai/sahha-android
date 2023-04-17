package sdk.sahha.android.domain.model.security

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
@Deprecated("Now using Android's encryption tool instead. This is only used for migration purposes.")
data class EncryptUtility(
    @PrimaryKey val alias: String,
    val iv: ByteArray,
    val encryptedData: ByteArray
)
