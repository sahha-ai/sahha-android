package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.source.SahhaNotificationConfiguration

interface SahhaConfigRepo {
    suspend fun saveConfig(config: SahhaConfiguration)
    suspend fun getConfig(): SahhaConfiguration
    suspend fun updateConfig(sensors: ArrayList<Int>)
    suspend fun saveNotificationConfig(notificationConfiguration: SahhaNotificationConfiguration)
    suspend fun getNotificationConfig(): SahhaNotificationConfiguration
    suspend fun saveDeviceInformation(deviceInformation: DeviceInformation)
    suspend fun getDeviceInformation(): DeviceInformation?
    suspend fun clearDeviceInformation()
}