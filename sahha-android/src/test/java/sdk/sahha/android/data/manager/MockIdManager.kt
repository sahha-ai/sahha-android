package sdk.sahha.android.data.manager

import sdk.sahha.android.domain.manager.IdManager

class MockIdManager: IdManager {
    private var deviceId: String? = null

    override fun getDeviceId(): String? {
        return deviceId
    }

    override fun saveDeviceId(id: String) {
        deviceId = id
    }
}