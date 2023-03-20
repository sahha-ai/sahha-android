package sdk.sahha.android.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sdk.sahha.android.common.*
import sdk.sahha.android.source.HealthConnectSensor
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.Constants.MAX_BATCH_POST_VALUE
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.data.local.dao.DeviceUsageDao
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.dto.DemographicDto
import sdk.sahha.android.data.remote.dto.SleepDto
import sdk.sahha.android.data.remote.dto.send.HeartRateSendDto
import sdk.sahha.android.data.remote.dto.send.SleepSendDto
import sdk.sahha.android.data.remote.dto.send.StepSendDto
import sdk.sahha.android.data.remote.dto.toSahhaDemographic
import sdk.sahha.android.domain.model.analyze.AnalyzeRequest
import sdk.sahha.android.domain.model.config.toSetOfSensors
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.repository.RemoteRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaSensor

class RemoteRepoImpl(
    private val sleepDao: SleepDao,
    private val deviceDao: DeviceUsageDao,
    private val movementDao: MovementDao,
    private val encryptor: Encryptor,
    private val decryptor: Decryptor,
    private val api: SahhaApi,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val mainScope: CoroutineScope,
    private val timeManager: SahhaTimeManager
) : RemoteRepo {

    private fun getFilteredStepDto(stepData: List<StepSendDto>): List<StepSendDto> {
        return if (stepData.count() > 1000) {
            stepData.subList(0, 1000)
        } else stepData
    }

    override suspend fun postStepData(
        stepData: List<StepSendDto>,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            if (stepData.isEmpty()) {
                callback?.also { it(SahhaErrors.localDataIsEmpty(SahhaSensor.pedometer), false) }
                return
            }

            val filteredStepDtoData = getFilteredStepDto(stepData)
            val response = getStepResponse(filteredStepDtoData)
            SahhaResponseHandler.handleResponse(
                response,
                { getStepResponse(filteredStepDtoData) },
                callback
            ) {
                if (stepData.count() > MAX_BATCH_POST_VALUE)
                    movementDao.clearFirstStepData(MAX_BATCH_POST_VALUE)
                else clearLocalStepData()
            }
        } catch (e: Exception) {
            callback?.also { it(e.message, false) }

            sahhaErrorLogger.application(
                e.message,
                "postStepData",
                stepData.toString()
            )
        }
    }

    override suspend fun postSleepData(
        sleepData: List<SleepDto>,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            if (sleepData.isEmpty()) {
                callback?.also { it(SahhaErrors.localDataIsEmpty(SahhaSensor.sleep), false) }
                return
            }

            val response = getSleepResponse()
            SahhaResponseHandler.handleResponse(response, { getSleepResponse() }, callback) {
                clearLocalSleepData()
            }
        } catch (e: Exception) {
            callback?.also { it(e.message, false) }

            sahhaErrorLogger.application(
                e.message,
                "postSleepData",
                sleepDao.getSleepDto().toString()
            )
        }
    }

    override suspend fun postHeartRateData(
        heartRateData: List<HeartRateSendDto>,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            if (heartRateData.isEmpty()) {
                callback?.invoke(
                    SahhaErrors.healthConnect.localDataIsEmpty(HealthConnectSensor.heart_rate),
                    false
                )
                return
            }

            val call = getHeartRateResponse(heartRateData)
            SahhaResponseHandler.handleResponse(
                call,
                { getHeartRateResponse(heartRateData) },
                callback
            )
        } catch (e: Exception) {
            callback?.invoke(e.message, false)

            sahhaErrorLogger.application(
                e.message,
                "postHeartRateData",
                heartRateData.toString()
            )
        }
    }

    override suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: Boolean) -> Unit)?) {
        try {
            if (deviceDao.getUsages().isEmpty()) {
                callback?.also { it(SahhaErrors.localDataIsEmpty(SahhaSensor.device), false) }
                return
            }

            val call = getPhoneScreenLockResponse()
            SahhaResponseHandler.handleResponse(call, { getPhoneScreenLockResponse() }, callback) {
                clearLocalPhoneScreenLockData()
            }
        } catch (e: Exception) {
            callback?.also { it(e.message, false) }

            sahhaErrorLogger.application(
                e.message,
                "postPhoneScreenLockData",
                deviceDao.getUsages().toString()
            )
        }
    }

    private suspend fun getHealthConnectSleepResponse(sleepSessionData: List<SleepSendDto>): Call<ResponseBody> {
        return api.postSleepDataRange(
            TokenBearer(decryptor.decrypt(UET)),
            sleepSessionData
        )
    }

    override suspend fun postAllSensorData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        try {
            postSensorData(callback)
        } catch (e: Exception) {
            callback(e.message, false)
            sahhaErrorLogger.application(
                e.message,
                "postAllSensorData",
                null
            )
        }
    }

    override suspend fun getAnalysis(
        dates: Pair<String, String>?,
        includeSourceData: Boolean,
        callback: ((error: String?, successful: String?) -> Unit)?,
    ) {
        try {
            val call = getDetectedAnalysisCall(dates, includeSourceData)
            call.enqueue(
                object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        mainScope.launch {
                            if (ResponseCode.isUnauthorized(response.code())) {
                                callback?.also { it(SahhaErrors.attemptingTokenRefresh, null) }
                                SahhaResponseHandler.checkTokenExpired(response.code()) {
                                    getAnalysis(dates, includeSourceData, callback)
                                }
                                return@launch
                            }

                            if (ResponseCode.isSuccessful(response.code())) {
                                SahhaResponseHandler.returnFormattedResponse(response, callback)
                                return@launch
                            }

                            callback?.also {
                                it(
                                    "${response.code()}: ${response.message()}",
                                    null
                                )
                            }

                            sahhaErrorLogger.api(call, response)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        callback?.also { it(t.message, null) }
                        sahhaErrorLogger.api(
                            call,
                            SahhaErrors.typeAuthentication,
                            null,
                            t.message ?: SahhaErrors.responseFailure
                        )
                    }
                }
            )
        } catch (e: Exception) {
            sahhaErrorLogger.application(
                e.message,
                "getAnalysis",
                dates?.toString()
            )
            callback?.also { it(e.message, null) }
        }
    }

    override suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        try {
            val call = getDemographicCall()
            call.enqueue(
                object : Callback<DemographicDto> {
                    override fun onResponse(
                        call: Call<DemographicDto>,
                        response: Response<DemographicDto>
                    ) {
                        mainScope.launch {
                            if (ResponseCode.isUnauthorized(response.code())) {
                                callback?.also { it(SahhaErrors.attemptingTokenRefresh, null) }
                                SahhaResponseHandler.checkTokenExpired(response.code()) {
                                    getDemographic(callback)
                                }

                                return@launch
                            }

                            if (ResponseCode.isSuccessful(response.code())) {
                                val sahhaDemographic = response.body()?.toSahhaDemographic()
                                callback?.also { it(null, sahhaDemographic) }

                                return@launch
                            }

                            callback?.also {
                                it(
                                    "${response.code()}: ${response.message()}",
                                    null
                                )
                            }

                            sahhaErrorLogger.api(call, response)
                        }
                    }

                    override fun onFailure(call: Call<DemographicDto>, t: Throwable) {
                        callback?.also { it(t.message, null) }
                        sahhaErrorLogger.api(
                            call,
                            SahhaErrors.typeResponse,
                            null,
                            t.message ?: SahhaErrors.responseFailure
                        )
                    }
                }
            )
        } catch (e: Exception) {
            callback?.also { it(e.message, null) }

            sahhaErrorLogger.application(
                e.message,
                "getDemographic",
                null
            )
        }
    }

    override suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            val call = postDemographicResponse(sahhaDemographic)
            call.enqueue(
                object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        mainScope.launch {
                            if (ResponseCode.isUnauthorized(response.code())) {
                                callback?.also { it(SahhaErrors.attemptingTokenRefresh, false) }
                                SahhaResponseHandler.checkTokenExpired(response.code()) {
                                    postDemographic(sahhaDemographic, callback)
                                }
                                return@launch
                            }

                            if (ResponseCode.isSuccessful(response.code())) {
                                callback?.also { it(null, true) }
                                return@launch
                            }

                            callback?.also {
                                it(
                                    "${response.code()}: ${response.message()}",
                                    false
                                )
                            }

                            sahhaErrorLogger.api(call, response)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        callback?.also { it(t.message, false) }
                        sahhaErrorLogger.api(
                            call,
                            SahhaErrors.typeResponse,
                            null,
                            t.message ?: SahhaErrors.responseFailure
                        )
                    }
                }
            )
        } catch (e: Exception) {
            callback?.also { it(e.message, false) }

            sahhaErrorLogger.application(
                e.message,
                "postDemographic",
                sahhaDemographic.toString()
            )
        }
    }

    private suspend fun postSensorData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        var errorSummary = ""
        val successfulResults = mutableListOf<Boolean>()

        val sensors = Sahha.di.configurationDao.getConfig().toSetOfSensors()
            .ifEmpty { SahhaSensor.values().toSet() }
        sensors.forEach { sensor ->
            if (sensor == SahhaSensor.sleep) {
                postSleepData(sleepDao.getSleepDto()) { error, successful ->
                    error?.also { errorSummary += "$it\n" }
                    successfulResults.add(successful)
                }
            }

            if (sensor == SahhaSensor.device) {
                postPhoneScreenLockData { error, successful ->
                    error?.also { errorSummary += "$it\n" }
                    successfulResults.add(successful)
                }
            }

            if (sensor == SahhaSensor.pedometer) {
                postStepData(
                    SahhaConverterUtility.stepDataToStepDto(movementDao.getAllStepData())
                ) { error, successful ->
                    error?.also { errorSummary += "$it\n" }
                    successfulResults.add(successful)
                }
            }
        }

        if (successfulResults.contains(false)) {
            callback(errorSummary, false)
            return
        }
        callback(null, true)
    }

    private suspend fun clearLocalStepData() {
        movementDao.clearAllStepData()
    }

    private suspend fun clearLocalSleepData() {
        sleepDao.clearSleepDto()
        sleepDao.clearSleep()
    }

    private suspend fun clearLocalPhoneScreenLockData() {
        deviceDao.clearUsages()
    }


    private suspend fun getStepResponse(stepData: List<StepSendDto>): Call<ResponseBody> {
        return api.postStepData(
            TokenBearer(decryptor.decrypt(UET)),
            stepData
        )
    }

    private suspend fun getSleepResponse(): Call<ResponseBody> {
        return api.postSleepDataRange(
            TokenBearer(decryptor.decrypt(UET)),
            SahhaConverterUtility.sleepDtoToSleepSendDto(sleepDao.getSleepDto())
        )
    }

    private suspend fun getHeartRateResponse(heartRateData: List<HeartRateSendDto>): Call<ResponseBody> {
        return api.postHeartRateRange(
            TokenBearer(decryptor.decrypt(UET)),
            heartRateData
        )
    }

    private suspend fun getPhoneScreenLockResponse(): Call<ResponseBody> {
        return api.postDeviceActivityRange(
            TokenBearer(decryptor.decrypt(UET)),
            SahhaConverterUtility.phoneUsageToPhoneUsageSendDto(deviceDao.getUsages())
        )
    }

    private suspend fun getAnalysisResponse(
        includeSourceData: Boolean
    ): Call<ResponseBody> {
        val analyzeRequest = AnalyzeRequest(
            null,
            null,
            includeSourceData
        )

        return api.analyzeProfile(TokenBearer(decryptor.decrypt(UET)), analyzeRequest)
    }

    private suspend fun getAnalysisResponse(
        startDate: String,
        endDate: String,
        includeSourceData: Boolean
    ): Call<ResponseBody> {
        val analyzeRequest = AnalyzeRequest(
            startDate,
            endDate,
            includeSourceData
        )


        return api.analyzeProfile(TokenBearer(decryptor.decrypt(UET)), analyzeRequest)
    }

    private suspend fun getDemographicCall(): Call<DemographicDto> {
        return api.getDemographic(TokenBearer(decryptor.decrypt(UET)))
    }

    private suspend fun postDemographicResponse(sahhaDemographic: SahhaDemographic): Call<ResponseBody> {
        return api.putDemographic(TokenBearer(decryptor.decrypt(UET)), sahhaDemographic)
    }

    private suspend fun getDetectedAnalysisCall(
        datesISO: Pair<String, String>?,
        includeSourceData: Boolean
    ): Call<ResponseBody> {
        return datesISO?.let { it ->
            getAnalysisResponse(it.first, it.second, includeSourceData)
        } ?: getAnalysisResponse(includeSourceData)
    }

    override suspend fun putDeviceInformation(deviceInformation: DeviceInformation) {
        val call = putDeviceInformationResponse(deviceInformation)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (ResponseCode.isSuccessful(response.code())) {
                    return
                }

                sahhaErrorLogger.api(call, response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                sahhaErrorLogger.api(
                    call,
                    SahhaErrors.typeResponse,
                    null,
                    t.message ?: SahhaErrors.responseFailure
                )
            }
        })
    }

    private suspend fun putDeviceInformationResponse(deviceInformation: DeviceInformation): Call<ResponseBody> {
        return api.putDeviceInformation(
            TokenBearer(decryptor.decrypt(UET)),
            SahhaConverterUtility.deviceInfoToDeviceInfoSendDto(deviceInformation)
        )
    }
}