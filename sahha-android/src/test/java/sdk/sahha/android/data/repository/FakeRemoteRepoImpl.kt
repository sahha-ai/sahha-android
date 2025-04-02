//package sdk.sahha.android.data.repository
//
//import androidx.health.connect.client.records.HeartRateRecord
//import androidx.health.connect.client.records.SleepSessionRecord
//import androidx.health.connect.client.records.SleepStageRecord
//import androidx.health.connect.client.records.StepsRecord
//import okhttp3.ResponseBody
//import retrofit2.Call
//import sdk.sahha.android.domain.model.SleepDto
//import sdk.sahha.android.domain.model.device_info.DeviceInformation
//import sdk.sahha.android.domain.model.steps.StepData
//import sdk.sahha.android.domain.repository.SensorRepo
//import sdk.sahha.android.domain.model.internal.SahhaDemographic
//import sdk.sahha.android.source.SahhaSensor
//
//class FakeSensorRepoImpl : SensorRepo {
//    var successful = false
//
//    override suspend fun postRefreshToken(retryLogic: suspend () -> Unit) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun postSleepData(
//        sleepData: List<SleepDto>,
//        callback: ((error: String?, successful: Boolean) -> Unit)?
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: Boolean) -> Unit)?) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun postStepData(
//        stepData: List<StepData>,
//        callback: ((error: String?, successful: Boolean) -> Unit)?
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun postHealthConnectData(
//        sleepSessionData: List<SleepSessionRecord>,
//        sleepStageData: List<SleepStageRecord>,
//        stepData: List<StepsRecord>,
//        heartRateData: List<HeartRateRecord>,
//        callback: ((error: String?, successful: Boolean) -> Unit)?
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun postAllSensorData(callback: (error: String?, successful: Boolean) -> Unit) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getAnalysis(
//        dates: Pair<String, String>?,
//        callback: ((error: String?, successful: String?) -> Unit)?
//    ) {
//        if (successful) {
//            dates?.also {
//                callback?.also {
//                    it(
//                        null,
//                        "{\"startDateTime\":\"${dates.first}\",\"endDateTime\":\"${dates.second}\"}"
//                    )
//                }
//            } ?: callback?.also { it(null, "{}") }
//            return
//        }
//
//        callback?.also { it("Failure", null) }
//    }
//
//    override suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun postDemographic(
//        sahhaDemographic: SahhaDemographic,
//        callback: ((error: String?, successful: Boolean) -> Unit)?
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun putDeviceInformation(deviceInformation: DeviceInformation) {
//        TODO("Not yet implemented")
//    }
//}