package sdk.sahha.android.controller.utils.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import sdk.sahha.android.data.ANDROID_KEY_STORE
import sdk.sahha.android.data.AppDatabase
import sdk.sahha.android.data.TRANSFORMATION
import sdk.sahha.android.model.security.EncryptUtility
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


// Credit to JosiasSena Github user
internal class Encryptor(private val context: Context) {
    private val securityDao by lazy { AppDatabase(context).database.securityDao() }
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