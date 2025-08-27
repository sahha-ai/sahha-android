package features.storage.infra.secure

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import core.SdkJson
import features.storage.domain.StorageKey
import features.storage.domain.StoragePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.text.Charsets.UTF_8

/**
 * ADAPTER: Android Keystore (AES-GCM) + SharedPreferences. Min API 23.
 * Moved behind the StoragePort so features depend only on the port.
 */
class SecureStoreAdapter(
    appContext: Context,
    private val json: Json = SdkJson.instance,
    private val keyAlias: String = StorageConfig.KEY_ALIAS,
    fileName: String = StorageConfig.SECURE_FILE
) : StoragePort {


    private val prefs = appContext.getSharedPreferences(fileName, Context.MODE_PRIVATE)


    override suspend fun <T> get(key: StorageKey<T>): T? = withContext(Dispatchers.IO) {
        val ivB64 =
            prefs.getString(key.name + StorageConfig.SUFFIX_IV, null) ?: return@withContext null
        val ctB64 =
            prefs.getString(key.name + StorageConfig.SUFFIX_CT, null) ?: return@withContext null
        val iv = b64d(ivB64)
        val ct = b64d(ctB64)


        runCatching {
            val cipher = Cipher.getInstance(StorageConfig.AES_TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(StorageConfig.GCM_TAG_BITS, iv)
            )
            val jsonStr = String(cipher.doFinal(ct), UTF_8)
            json.decodeFromString(key.serializer, jsonStr)
        }.onFailure {
            remove(key) // wipe inconsistent entry (e.g., key invalidated)
        }.getOrNull()
    }


    override suspend fun <T> put(key: StorageKey<T>, value: T?) = withContext(Dispatchers.IO) {
        if (value == null) {
            remove(key); return@withContext
        }
        val plaintext = json.encodeToString(key.serializer, value).toByteArray(UTF_8)


        val cipher = Cipher.getInstance(StorageConfig.AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val ct = cipher.doFinal(plaintext)


        prefs.edit {
            putString(key.name + StorageConfig.SUFFIX_IV, b64(iv))
            putString(key.name + StorageConfig.SUFFIX_CT, b64(ct))
        }
    }


    override suspend fun remove(key: StorageKey<*>) = withContext(Dispatchers.IO) {
        prefs.edit {
            remove(key.name + StorageConfig.SUFFIX_IV)
            remove(key.name + StorageConfig.SUFFIX_CT)
        }
    }


    override suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit { clear() }
    }


// --- Keystore helpers ---


    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(keyAlias, null) as? SecretKey)?.let { return it }


        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false)
            .build()
        gen.init(spec)
        return gen.generateKey()
    }


    private fun b64(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE)

    private fun b64d(text: String): ByteArray =
        Base64.decode(text, Base64.NO_WRAP or Base64.URL_SAFE)
}