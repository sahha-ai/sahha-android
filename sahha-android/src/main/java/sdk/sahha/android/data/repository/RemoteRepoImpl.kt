package sdk.sahha.android.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import sdk.sahha.android.common.*
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.Constants.API_ERROR
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
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Named

class RemoteRepoImpl @Inject constructor(
    @Named("ioScope") private val ioScope: CoroutineScope,
    private val sleepDao: SleepDao,
    private val deviceDao: DeviceUsageDao,
    private val encryptor: Encryptor,
    private val decryptor: Decryptor,
    private val api: SahhaApi,
    private val appCenterLog: AppCenterLog
) : RemoteRepo {
    private val tag = "RemoteRepoImpl"

    override suspend fun postRefreshToken(retryLogic: (suspend () -> Unit)) {
        Log.d(tag, "postRefreshToken")
        val tokenData = TokenData(
            decryptor.decrypt(UET),
            decryptor.decrypt(UERT)
        )

        try {
            Log.d(tag, "try")
            val response = getRefreshTokenResponse(tokenData)
            if (ResponseCode.isSuccessful(response.code())) {
                Log.d(tag, "ResponseCode.isSuccessful")
                storeNewTokens(response.body())
                retryLogic()
                return
            }
            appCenterLog.api(API_ERROR, false, null, SahhaErrors.typeAuthentication)
        } catch (e: Exception) {
            appCenterLog.application(e.message ?: "Error refreshing token")
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
        dates: Pair<LocalDateTime, LocalDateTime>?,
        callback: ((error: String?, successful: String?) -> Unit)?,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            callback?.also {
                it(SahhaErrors.androidVersionTooLow(8), null)
            }
            return
        }

        try {
            val response = getDetectedAnalysisResponse(dates)

            if (ResponseCode.isUnauthorized(response.code())) {
                callback?.also { it(SahhaErrors.attemptingTokenRefresh, null) }
                checkTokenExpired(response.code()) {
                    getAnalysis(dates, callback)
                }
                return
            }

            if (ResponseCode.isSuccessful(response.code())) {
                returnFormattedResponse(response, callback)
                return
            }

            callback?.also { it("${response.code()}: ${response.message()}", null) }
        } catch (e: Exception) {
            callback?.also { it(e.message, null) }
        }
    }

    override suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        try {
            val response = getDemographicResponse()

            if (ResponseCode.isUnauthorized(response.code())) {
                callback?.also { it(SahhaErrors.attemptingTokenRefresh, null) }
                checkTokenExpired(response.code()) {
                    getDemographic(callback)
                }
                return
            }

            if (ResponseCode.isSuccessful(response.code())) {
                val sahhaDemographic = response.body()?.toSahhaDemographic()
                callback?.also { it(null, sahhaDemographic) }
                return
            }

            callback?.also { it("${response.code()}: ${response.message()}", null) }
        } catch (e: Exception) {
            callback?.also { it(e.message, null) }
        }
    }

    override suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        try {
            val response = postDemographicResponse(sahhaDemographic)
            if (ResponseCode.isUnauthorized(response.code())) {
                callback?.also { it(SahhaErrors.attemptingTokenRefresh, false) }
                checkTokenExpired(response.code()) {
                    postDemographic(sahhaDemographic, callback)
                }
                return
            }

            if (ResponseCode.isSuccessful(response.code())) {
                callback?.also { it(null, true) }
                return
            }

            callback?.also { it("${response.code()}: ${response.message()}", false) }
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
        retryLogic: suspend (() -> Response<ResponseBody>),
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
        Log.d(tag, "storeNewTokens")
        val json = ApiBodyConverter.responseBodyToJson(responseBody)
        json?.also {
            Log.d(tag, "json?.also")
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

    private suspend fun getRefreshTokenResponse(
        td: TokenData
    ): Response<ResponseBody> {
        return api.postRefreshToken(td)
    }

    private suspend fun getSleepResponse(): Response<ResponseBody> {
        return api.postSleepDataRange(
            TokenBearer(decryptor.decrypt(UET)),
            sleepDao.getSleepDto()
        )
    }

    private suspend fun getPhoneScreenLockResponse(): Response<ResponseBody> {
        return api.postDeviceActivityRange(
            TokenBearer(decryptor.decrypt(UET)),
            deviceDao.getUsages()
        )
    }

    private suspend fun getAnalysisResponse(): Response<ResponseBody> {
        return api.analyzeProfile(TokenBearer(decryptor.decrypt(UET)))
    }

    private suspend fun getAnalysisResponse(
        startDate: String,
        endDate: String
    ): Response<ResponseBody> {
        return api.analyzeProfile(TokenBearer(decryptor.decrypt(UET)), startDate, endDate)
    }

    private suspend fun getDemographicResponse(): Response<DemographicDto> {
        return api.getDemographic(TokenBearer(decryptor.decrypt(UET)))
    }

    private suspend fun postDemographicResponse(sahhaDemographic: SahhaDemographic): Response<ResponseBody> {
        return api.postDemographic(TokenBearer(decryptor.decrypt(UET)), sahhaDemographic)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getDetectedAnalysisResponse(dates: Pair<LocalDateTime, LocalDateTime>?): Response<ResponseBody> {
        return dates?.let { it ->
            val sahhaTimeManager = SahhaTimeManager()
            val startDate = sahhaTimeManager.localDateTimeToISO(it.first)
            val endDate = sahhaTimeManager.localDateTimeToISO(it.second)
            getAnalysisResponse(startDate, endDate)
        } ?: getAnalysisResponse()
    }
}