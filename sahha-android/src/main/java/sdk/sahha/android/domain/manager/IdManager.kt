package sdk.sahha.android.domain.manager

internal interface IdManager {
    fun getDeviceId(): String?
    fun saveDeviceId(id: String)
}