package features.storage.infra.prefs

import android.content.Context
import androidx.core.content.edit
import core.SdkJson
import features.storage.domain.StorageKey
import features.storage.domain.StoragePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Plain SharedPreferences adapter for non-sensitive values. */
class PrefsStoreAdapter(
    appContext: Context,
    private val json: Json = SdkJson.instance,
    private val fileName: String = "sahha_prefs_store"
) : StoragePort {


    private val prefs = appContext.getSharedPreferences(fileName, Context.MODE_PRIVATE)


    override suspend fun <T> get(key: StorageKey<T>): T? = withContext(Dispatchers.IO) {
        val jsonStr = prefs.getString(key.name, null) ?: return@withContext null
        runCatching { json.decodeFromString(key.serializer, jsonStr) }.getOrNull()
    }


    override suspend fun <T> put(key: StorageKey<T>, value: T?) = withContext(Dispatchers.IO) {
        if (value == null) {
            remove(key); return@withContext
        }
        val encoded = json.encodeToString(key.serializer, value)
        prefs.edit { putString(key.name, encoded) }
    }


    override suspend fun remove(key: StorageKey<*>) = withContext(Dispatchers.IO) {
        prefs.edit { remove(key.name) }
    }


    override suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit { clear() }
    }
}