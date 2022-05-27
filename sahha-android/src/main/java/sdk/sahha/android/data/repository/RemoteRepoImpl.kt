package sdk.sahha.android.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sdk.sahha.android.common.*
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.Constants.UERT
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.data.local.dao.DeviceUsageDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.dto.DemographicDto
import sdk.sahha.android.data.remote.dto.toSahhaDemographic
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.repository.RemoteRepo
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaSensor
import javax.inject.Inject
import javax.inject.Named

class RemoteRepoImpl @Inject constructor(
    private val sleepDao: SleepDao,
    private val deviceDao: DeviceUsageDao,
    private val encryptor: Encryptor,
    private val decryptor: Decryptor,
    private val api: SahhaApi,
    private val sahhaErrorLogger: SahhaErrorLogger,
    @Named("ioScope") private val ioScope: CoroutineScope
) : RemoteRepo {

    override suspend fun postRefreshToken(retryLogic: (suspend () -> Unit)) {
        val tokenData = TokenData(
            decryptor.decrypt(UET),
            decryptor.decrypt(UERT)
        )

        try {
            val call = getRefreshTokenCall(tokenData)
            call.enqueue(
                object: Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        ioScope.launch {
                            if (ResponseCode.isSuccessful(response.code())) {
                                storeNewTokens(response.body())
                                retryLogic()
                                return@launch
                            }

                            sahhaErrorLogger.api(
                                call,
                                SahhaErrors.typeAuthentication,
                                response.code(),
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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
                e.message ?: "Error refreshing token",
                "postRefreshToken",
                null
            )
        }
    }

    override suspend fun postSleepData(callback: ((error: String?, successful: Boolean) -> Unit)?) {
        try {
            if (sleepDao.getSleepDto().isEmpty()) {
                callback?.also { it(SahhaErrors.localDataIsEmpty(SahhaSensor.sleep), false) }
                return
            }

            val response = getSleepResponse()
            handleResponse(response, { getSleepResponse() }, callback) {
                clearLocalSleepData()
            }
        } catch (e: Exception) {
            callback?.also { it(e.message, false) }
        }
    }

    override suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: Boolean) -> Unit)?) {
        try {
            if (deviceDao.getUsages().isEmpty()) {
                callback?.also { it(SahhaErrors.localDataIsEmpty(SahhaSensor.device), false) }
                return
            }

            val response = getPhoneScreenLockResponse()
            handleResponse(response, { getPhoneScreenLockResponse() }, callback) {
                clearLocalPhoneScreenLockData()
            }
        } catch (e: Exception) {
            callback?.also { it(e.message, false) }
        }
    }

    override suspend fun postAllSensorData(
        sensors: Set<Enum<SahhaSensor>>?,
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        try {
            postSensorData(sensors, callback)
        } catch (e: Exception) {
            callback(e.message, false)
        }
    }

    override suspend fun getAnalysis(
        dates: Pair<String, String>?,
        callback: ((error: String?, successful: String?) -> Unit)?,
    ) {
        try {
            val call = getDetectedAnalysisCall(dates)
            call.enqueue(
                object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        ioScope.launch {
                            if (ResponseCode.isUnauthorized(response.code())) {
                                callback?.also { it(SahhaErrors.attemptingTokenRefresh, null) }
                                checkTokenExpired(response.code()) {
                                    getAnalysis(dates, callback)
                                }

                                return@launch
                            }

                            if (ResponseCode.isSuccessful(response.code())) {
                                returnFormattedResponse(response, callback)
                                return@launch
                            }

                            callback?.also { it("${response.code()}: ${response.message()}", null) }

                            sahhaErrorLogger.api(
                                call,
                                SahhaErrors.typeAuthentication,
                                response.code(),
                                response.message()
                            )
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
            callback?.also { it(e.message, null) }
        }
    }

    override suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        try {
            val call = getDemographicCall()
            call.enqueue(
                object: Callback<DemographicDto> {
                    override fun onResponse(
                        call: Call<DemographicDto>,
                        response: Response<DemographicDto>
                    ) {
                        ioScope.launch {
                            if (ResponseCode.isUnauthorized(response.code())) {
                                callback?.also { it(SahhaErrors.attemptingTokenRefresh, null) }
                                checkTokenExpired(response.code()) {
                                    getDemographic(callback)
                                }

                                return@launch
                            }

                            if (ResponseCode.isSuccessful(response.code())) {
                                val sahhaDemographic = response.body()?.toSahhaDemographic()
                                callback?.also { it(null, sahhaDemographic) }

                                return@launch
                            }

                            callback?.also { it("${response.code()}: ${response.message()}", null) }

                            sahhaErrorLogger.api(
                                call,
                                SahhaErrors.typeAuthentication,
                                response.code(),
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<DemographicDto>, t: Throwable) {
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
        }
    }

    override suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            val call = postDemographicResponse(sahhaDemographic)
            call.enqueue(
                object: Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        ioScope.launch {
                            if (ResponseCode.isUnauthorized(response.code())) {
                                callback?.also { it(SahhaErrors.attemptingTokenRefresh, false) }
                                checkTokenExpired(response.code()) {
                                    postDemographic(sahhaDemographic, callback)
                                }

                                sahhaErrorLogger.api(
                                    call,
                                    SahhaErrors.typeAuthentication,
                                    response.code(),
                                    response.message()
                                )
                                return@launch
                            }

                            if (ResponseCode.isSuccessful(response.code())) {
                                callback?.also { it(null, true) }
                                return@launch
                            }

                            callback?.also { it("${response.code()}: ${response.message()}", false) }

                            sahhaErrorLogger.api(
                                call,
                                SahhaErrors.typeAuthentication,
                                response.code(),
                                response.message()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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
        }
    }

    private fun returnFormattedResponse(
        response: Response<ResponseBody>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        if (response.code() == 204) {
            callback?.also { it(null, "{}") }
            return
        }

        val reader = response.body()?.charStream()
        val bodyString = reader?.readText()
        val json = JSONObject(bodyString ?: "")
        val jsonString = json.toString(6)
        callback?.also { it(null, jsonString) }
    }

    private suspend fun handleResponse(
        response: Response<ResponseBody>,
        retryLogic: suspend (() -> Call<ResponseBody>),
        callback: ((error: String?, successful: Boolean) -> Unit)?,
        successfulLogic: (suspend () -> Unit)
    ) {
        if (ResponseCode.isUnauthorized(response.code())) {
            callback?.also { it(SahhaErrors.attemptingTokenRefresh, false) }
            checkTokenExpired(response.code()) {
                val retryResponse = retryLogic()
                handleResponse(
                    retryResponse,
                    retryLogic,
                    callback,
                    successfulLogic
                )
            }
            return
        }

        if (ResponseCode.isSuccessful(response.code())) {
            successfulLogic()
            callback?.also {
                it(null, true)
            }
            return
        }

        callback?.also {
            it("${response.code()}: ${response.message()}", false)
        }
    }

    private suspend fun checkTokenExpired(
        code: Int,
        retryLogic: suspend () -> Unit
    ) {
        if (ResponseCode.isUnauthorized(code)) {
            postRefreshToken(retryLogic)
        }
    }

    private suspend fun postSensorData(
        sensors: Set<Enum<SahhaSensor>>?,
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        var errorSummary = ""
        val successfulResults = mutableListOf<Boolean>()

        val specificOrAllSensors = sensors ?: SahhaSensor.values().toSet()
        specificOrAllSensors.forEach { sensor ->
            if (sensor == SahhaSensor.sleep) {
                postSleepData { error, successful ->
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
        }

        if (successfulResults.contains(false)) {
            callback(errorSummary, false)
            return
        }
        callback(null, true)
    }

    private suspend fun storeNewTokens(responseBody: ResponseBody?) {
        val json = ApiBodyConverter.responseBodyToJson(responseBody)
        json?.also {
            encryptor.encryptText(UET, it["profileToken"].toString())
            encryptor.encryptText(UERT, it["refreshToken"].toString())
        }
    }

    private suspend fun clearLocalSleepData() {
        sleepDao.clearSleepDto()
        sleepDao.clearSleep()
    }

    private suspend fun clearLocalPhoneScreenLockData() {
        deviceDao.clearUsages()
    }

    private suspend fun getRefreshTokenCall(
        td: TokenData
    ): Call<ResponseBody> {
        return api.postRefreshToken(td)
    }

    private suspend fun getSleepResponse(): Call<ResponseBody> {
        return api.postSleepDataRange(
            TokenBearer(decryptor.decrypt(UET)),
            sleepDao.getSleepDto()
        )
    }

    private suspend fun getPhoneScreenLockResponse(): Call<ResponseBody> {
        return api.postDeviceActivityRange(
            TokenBearer(decryptor.decrypt(UET)),
            deviceDao.getUsages()
        )
    }

    private suspend fun getAnalysisResponse(): Call<ResponseBody> {
        return api.analyzeProfile(TokenBearer(decryptor.decrypt(UET)))
    }

    private suspend fun getAnalysisResponse(
        startDate: String,
        endDate: String
    ): Call<ResponseBody> {
        return api.analyzeProfile(TokenBearer(decryptor.decrypt(UET)), startDate, endDate)
    }

    private suspend fun getDemographicCall(): Call<DemographicDto> {
        return api.getDemographic(TokenBearer(decryptor.decrypt(UET)))
    }

    private suspend fun postDemographicResponse(sahhaDemographic: SahhaDemographic): Call<ResponseBody> {
        return api.postDemographic(TokenBearer(decryptor.decrypt(UET)), sahhaDemographic)
    }

    private suspend fun getDetectedAnalysisCall(datesISO: Pair<String, String>?): Call<ResponseBody> {
        return datesISO?.let { it ->
            getAnalysisResponse(it.first, it.second)
        } ?: getAnalysisResponse()
    }
}