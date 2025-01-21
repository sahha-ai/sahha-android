package sdk.sahha.android.data.manager

import android.content.SharedPreferences
import sdk.sahha.android.domain.manager.IdManager
import javax.inject.Inject

const val DEVICE_ID = "device_id"

internal class IdManagerImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : IdManager {
    override fun getDeviceId(): String? {
        return sharedPreferences.getString(DEVICE_ID, null)
    }

    override fun saveDeviceId(id: String) {
        sharedPreferences.edit().putString(DEVICE_ID, id).apply()
    }
}