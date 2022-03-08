package sdk.sahha.android.controller.utils.security

import android.content.Context
import sdk.sahha.android.data.ANDROID_KEY_STORE
import sdk.sahha.android.data.AppDatabase
import sdk.sahha.android.data.TRANSFORMATION
import sdk.sahha.android.data.UET
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// Credit to JosiasSena Github user
class Decryptor(private val context: Context) {
    private val securityDao by lazy { AppDatabase(context).database.securityDao() }
    private var keyStore: KeyStore? = null

    init {
        initKeyStore()
    }

    private fun initKeyStore() {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
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

    private suspend fun decryptData(alias: String): String {
        val encryption = securityDao.getEncryptUtility(alias)
        val encryptionIv = encryption.iv

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, encryptionIv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(alias), spec)
        return cipher.doFinal(encryption.encryptedData).decodeToString()
    }

    private fun getSecretKey(alias: String): SecretKey? {
        return (keyStore!!.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
    }
}