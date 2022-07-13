package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.DeviceInfoRepo

class GetPlatformUseCase (
    private val repository: DeviceInfoRepo
){
    operator fun invoke() {
        repository.getPlatform()
    }
}