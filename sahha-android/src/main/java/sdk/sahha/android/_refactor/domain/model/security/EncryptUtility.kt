package sdk.sahha.android._refactor.domain.model.security

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class EncryptUtility(
    @PrimaryKey val alias: String,
    val iv: ByteArray,
    val encryptedData: ByteArray
)
