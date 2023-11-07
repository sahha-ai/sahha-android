package sdk.sahha.android.common.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import sdk.sahha.android.common.Constants.ANDROID_KEY_STORE
import sdk.sahha.android.common.Constants.TRANSFORMATION
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.domain.model.security.EncryptUtility
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


// Credit to JosiasSena Github user
class Encryptor (
    private val securityDao: SecurityDao
) {
    private lateinit var encryption: ByteArray

    // To test commit

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