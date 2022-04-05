package sdk.sahha.android.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.security.Decryptor
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
        if (sleepDao.getSleepDto().isEmpty()) {
            callback?.let { it(SahhaErrors.localDataIsEmpty(SahhaSensor.SLEEP), null) }
            return
        }

        val call = getSleepCall()
        enqueueCall(call, callback) {
            ioScope.launch {
                clearLocalSleepData()
            }
        }
    }

    override suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: String?) -> Unit)?) {
        if (deviceDao.getUsages().isEmpty()) {
            callback?.let { it(SahhaErrors.localDataIsEmpty(SahhaSensor.DEVICE), null) }
            return
        }

        val call = getPhoneScreenLockCall()
        enqueueCall(call, callback) {
            ioScope.launch {
                clearLocalPhoneScreenLockData()
            }
        }
    }

    override suspend fun getAnalysis(callback: ((error: String?, successful: String?) -> Unit)?) {
        val response = getAnalysisResponse()

        if (ResponseCode.isSuccessful(response.code())) {
            returnFormattedResponse(callback, response.body())
            return
        }

        callback?.let { it("${response.code()}: ${response.message()}", null) }
    }

    override suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        val response = getDemographicResponse()

        if (ResponseCode.isSuccessful(response.code())) {
            val sahhaDemographic = response.body()?.toSahhaDemographic()
            callback?.let { it(null, sahhaDemographic) }
            return
        }

        callback?.let { it("${response.code()}: ${response.message()}", null) }
    }

    override suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: String?) -> Unit)?
    ) {
        val response = postDemographicResponse(sahhaDemographic)

        if (ResponseCode.isSuccessful(response.code())) {
            callback?.let { it(null, "${response.code()}: ${response.message()}") }
            return
        }

        callback?.let { it("${response.code()}: ${response.message()}", null) }
    }

    private fun returnFormattedResponse(
        callback: ((error: String?, success: String?) -> Unit)?,
        responseBody: ResponseBody?
    ) {
        val reader = responseBody?.charStream()
        val bodyString = reader?.readText()
        val json = JSONObject(bodyString ?: "")
        val jsonString = json.toString(6)
        callback?.let { it(null, jsonString) }
    }

    private fun enqueueCall(
        call: Call<ResponseBody>,
        callback: ((error: String?, successful: String?) -> Unit)?,
        successfulLogic: (() -> Unit)
    ) {
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (ResponseCode.isSuccessful(response.code())) {
                    successfulLogic()
                    callback?.let {
                        it(null, "${response.code()}: ${response.message()}")
                    }
                    return
                }

                callback?.let {
                    it("${response.code()}: ${response.message()}", null)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback?.let {
                    it(t.message, null)
                }
            }
        })
    }

    private suspend fun clearLocalSleepData() {
        sleepDao.clearSleepDto()
        sleepDao.clearSleep()
    }

    private suspend fun clearLocalPhoneScreenLockData() {
        deviceDao.clearUsages()
    }

    private suspend fun getSleepCall(): Call<ResponseBody> {
        return api.postSleepDataRange(
            decryptor.decryptToken(),
            sleepDao.getSleepDto()
        )
    }

    private suspend fun getPhoneScreenLockCall(): Call<ResponseBody> {
        return api.postDeviceActivityRange(
            decryptor.decryptToken(),
            deviceDao.getUsages()
        )
    }

    private suspend fun getAnalysisResponse(): Response<ResponseBody> {
        return api.analyzeProfile(decryptor.decryptToken())
    }

    private suspend fun getDemographicResponse(): Response<DemographicDto> {
        return api.getDemographic(decryptor.decryptToken())
    }

    private suspend fun postDemographicResponse(sahhaDemographic: SahhaDemographic): Response<ResponseBody> {
        return api.postDemographic(decryptor.decryptToken(), sahhaDemographic)
    }
}