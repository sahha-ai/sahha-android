package sdk.sahha.android.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.data.local.dao.DeviceUsageDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.repository.RemotePostRepo
import javax.inject.Inject
import javax.inject.Named

class RemotePostRepoImpl @Inject constructor(
    @Named("ioScope") private val ioScope: CoroutineScope,
    private val sleepDao: SleepDao,
    private val deviceDao: DeviceUsageDao,
    private val decryptor: Decryptor,
    private val api: SahhaApi
) : RemotePostRepo {
    override suspend fun postSleepData(callback: ((error: String?, successful: String?) -> Unit)?) {
        val call = getSleepCall()
        enqueueCall(call, callback) {
            ioScope.launch {
                clearLocalSleepData()
            }
        }
    }

    override suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: String?) -> Unit)?) {
        val call = getPhoneScreenLockCall()
        enqueueCall(call, callback) {
            ioScope.launch {
                clearLocalPhoneScreenLockData()
            }
        }
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
        return api.sendSleepDataRange(
            decryptor.decryptToken(),
            sleepDao.getSleepDto()
        )
    }

    private suspend fun getPhoneScreenLockCall(): Call<ResponseBody> {
        return api.sendDeviceActivityRange(
            decryptor.decryptToken(),
            deviceDao.getUsages()
        )
    }
}