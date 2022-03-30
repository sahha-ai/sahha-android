package sdk.sahha.android.common.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.domain.model.security.EncryptUtility
import sdk.sahha.android.data.Constants.ANDROID_KEY_STORE
import sdk.sahha.android.data.Constants.TRANSFORMATION
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject


// Credit to JosiasSena Github user
class Encryptor @Inject constructor(
    private val securityDao: SecurityDao
) {
    private lateinit var encryption: ByteArray

    suspend fun encryptText(alias: String, textToEncrypt: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(alias))

        val encrypted =
            cipher.doFinal(textToEncrypt.toByteArray(charset("UTF-8"))).also { encryption = it }


        securityDao.saveEncryptUtility(
            EncryptUtility(
                alias, cipher.iv, encrypted
            )
        )
    }

    private fun getSecretKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }
}