package sdk.sahha.android.common.security

import sdk.sahha.android.data.Constants.ANDROID_KEY_STORE
import sdk.sahha.android.data.Constants.TRANSFORMATION
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.data.local.dao.SecurityDao
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

// Credit to JosiasSena Github user
class Decryptor @Inject constructor(
    private val securityDao: SecurityDao
) {
    private val keyStore by lazy { KeyStore.getInstance(ANDROID_KEY_STORE) }

    init {
        keyStore?.load(null)
    }

    suspend fun decryptToken(): String {
        val encryption = securityDao.getEncryptUtility(UET)
        val encryptionIv = encryption.iv

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, encryptionIv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(UET), spec)
        return cipher.doFinal(encryption.encryptedData).decodeToString()
    }

    private fun getSecretKey(alias: String): SecretKey? {
        return (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
    }
}