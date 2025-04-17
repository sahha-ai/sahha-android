package sdk.sahha.android.data.repository

import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.source.SahhaNotificationConfiguration

internal class MockSahhaConfigRepo: SahhaConfigRepo {
    private var configs = mutableListOf<SahhaConfiguration>()
    private var notificationConfigs = mutableListOf<SahhaNotificationConfiguration>()
    private var deviceInformations = mutableListOf<DeviceInformation>()

    override suspend fun saveConfig(config: SahhaConfiguration) {
        configs = mutableListOf(config)
    }

    override suspend fun getConfig(): SahhaConfiguration {
        return configs.first()
    }

    override suspend fun updateConfig(sensors: ArrayList<Int>) {
        configs = mutableListOf(configs.first().copy(sensorArray = sensors))
    }

    override suspend fun saveNotificationConfig(notificationConfiguration: SahhaNotificationConfiguration) {
        notificationConfigs = mutableListOf(notificationConfiguration)
    }

    override suspend fun getNotificationConfig(): SahhaNotificationConfiguration {
        return notificationConfigs.first()
    }

    override suspend fun saveDeviceInformation(deviceInformation: DeviceInformation) {
        deviceInformations = mutableListOf(deviceInformation)
    }

    override suspend fun getDeviceInformation(): DeviceInformation? {
        return deviceInformations.first()
    }

    override suspend fun clearDeviceInformation() {
        deviceInformations.clear()
    }
}