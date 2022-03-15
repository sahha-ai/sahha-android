package sdk.sahha.android.domain.use_case

import sdk.sahha.android.domain.repository.DeviceInfoRepo
import javax.inject.Inject

class GetPlatformUseCase @Inject constructor(
    private val repository: DeviceInfoRepo
){
    operator fun invoke() {
        repository.getPlatform()
    }
}