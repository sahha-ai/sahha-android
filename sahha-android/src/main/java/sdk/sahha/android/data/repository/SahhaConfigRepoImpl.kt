package sdk.sahha.android.data.repository

import android.content.SharedPreferences
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.source.SahhaNotificationConfiguration
import javax.inject.Inject

internal class SahhaConfigRepoImpl @Inject constructor(
    private val dao: ConfigurationDao,
) : SahhaConfigRepo {
    override suspend fun saveConfig(config: SahhaConfiguration) {
        dao.saveConfig(config)
    }

    override suspend fun getConfig(): SahhaConfiguration {
        return dao.getConfig()
    }

    override suspend fun updateConfig(sensors: ArrayList<Int>) {
        dao.updateConfig(sensors)
    }

    override suspend fun saveNotificationConfig(notificationConfiguration: SahhaNotificationConfiguration) {
        dao.saveNotificationConfig(notificationConfiguration)
    }

    override suspend fun getNotificationConfig(): SahhaNotificationConfiguration {
        return dao.getNotificationConfig()
    }

    override suspend fun saveDeviceInformation(deviceInformation: DeviceInformation) {
        dao.saveDeviceInformation(deviceInformation)
    }

    override suspend fun getDeviceInformation(): DeviceInformation? {
        return dao.getDeviceInformation()
    }

    override suspend fun clearDeviceInformation() {
        dao.clearDeviceInformation()
    }
}