package sdk.sahha.android.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.data.local.dao.DeviceUsageDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.data.remote.dto.DemographicDto
import sdk.sahha.android.domain.model.enums.SahhaSensor
import sdk.sahha.android.domain.model.profile.SahhaDemographic
import sdk.sahha.android.domain.repository.RemoteRepo
import javax.inject.Inject
import javax.inject.Named

class RemoteRepoImpl @Inject constructor(
    @Named("ioScope") private val ioScope: CoroutineScope,
    private val sleepDao: SleepDao,
    private val deviceDao: DeviceUsageDao,
    private val decryptor: Decryptor,
    private val api: SahhaApi
) : RemoteRepo {
    override suspend fun postSleepData(callback: ((error: String?, successful: String?) -> Unit)?) {
        try {
            if (sleepDao.getSleepDto().isEmpty()) {
                callback?.also { it(SahhaErrors.localDataIsEmpty(SahhaSensor.SLEEP), null) }
                return
            }

            val response = getSleepResponse()
            handleResponse(response, callback) {
                ioScope.launch {
                    clearLocalSleepData()
                }
            }
        } catch (e: Exception) {
            callback?.also { it(e.message, null) }
        }
    }

    override suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: String?) -> Unit)?) {
        try {
            if (deviceDao.getUsages().isEmpty()) {
                callback?.also { it(SahhaErrors.localDataIsEmpty(SahhaSensor.DEVICE), null) }
                return
            }

            val response = getPhoneScreenLockResponse()
            handleResponse(response, callback) {
                ioScope.launch {
                    clearLocalPhoneScreenLockData()
                }
            }
        } catch (e: Exception) {
            callback?.also { it(e.message, null) }
        }
    }

    override suspend fun getAnalysis(callback: ((error: String?, successful: String?) -> Unit)?) {
        try {
            val response = getAnalysisResponse()

            if (ResponseCode.isSuccessful(response.code())) {
                returnFormattedResponse(callback, response.body())
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
        callback: ((error: String?, successful: String?) -> Unit)?
    ) {
        try {
            val response = postDemographicResponse(sahhaDemographic)

            if (ResponseCode.isSuccessful(response.code())) {
                callback?.also { it(null, "${response.code()}: ${response.message()}") }
                return
            }

            callback?.also { it("${response.code()}: ${response.message()}", null) }
        } catch (e: Exception) {
            callback?.also { it(e.message, null) }
        }
    }

    private fun returnFormattedResponse(
        callback: ((error: String?, success: String?) -> Unit)?,
        responseBody: ResponseBody?
    ) {
        val reader = responseBody?.charStream()
        val bodyString = reader?.readText()
        val json = JSONObject(bodyString ?: "")
        val jsonString = json.toString(6)
        callback?.also { it(null, jsonString) }
    }

    private fun handleResponse(
        response: Response<ResponseBody>,
        callback: ((error: String?, successful: String?) -> Unit)?,
        successfulLogic: (() -> Unit)
    ) {
        if (ResponseCode.isSuccessful(response.code())) {
            successfulLogic()
            callback?.also {
                it(null, "${response.code()}: ${response.message()}")
            }
            return
        }

        callback?.also {
            it("${response.code()}: ${response.message()}", null)
        }
    }

    private suspend fun clearLocalSleepData() {
        sleepDao.clearSleepDto()
        sleepDao.clearSleep()
    }

    private suspend fun clearLocalPhoneScreenLockData() {
        deviceDao.clearUsages()
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

    private suspend fun getDemographicResponse(): Response<DemographicDto> {
        return api.getDemographic(TokenBearer(decryptor.decrypt(UET)))
    }

    private suspend fun postDemographicResponse(sahhaDemographic: SahhaDemographic): Response<ResponseBody> {
        return api.postDemographic(TokenBearer(decryptor.decrypt(UET)), sahhaDemographic)
    }
}