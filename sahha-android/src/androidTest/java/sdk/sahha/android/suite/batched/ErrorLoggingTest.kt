package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.common.appId
import sdk.sahha.android.common.appSecret
import sdk.sahha.android.common.externalId
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaFramework
import sdk.sahha.android.source.SahhaScoreType
import sdk.sahha.android.source.SahhaSettings
import java.time.LocalDateTime
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
class ErrorLoggingTest {
    companion object {
        lateinit var activity: ComponentActivity

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            val settings = SahhaSettings(SahhaEnvironment.sandbox)
            SahhaSetupUtil.configureSahha(activity, settings)
        }
    }

    @Test
    fun errorLogs_postSuccessfully() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        suspendCoroutine { cont ->
            Sahha.postError(
                SahhaFramework.android_kotlin,
                "Test error message",
                "ErrorLoggingTest",
                "errorPostTest"
            ) { error, success ->
                Assert.assertEquals(null, error)
                Assert.assertEquals(true, success)
                cont.resume(Unit)
            }
        }
    }

    @Test
    fun noAuth_postsSuccessfully() = runTest {
        SahhaSetupUtil.deauthenticateSahha()
        suspendCoroutine { cont ->
            Sahha.postError(
                SahhaFramework.android_kotlin,
                "Some test message",
                "ErrorLoggingTest",
                "noAuthTest"
            ) { postError, postSuccess ->
                Assert.assertEquals(true, postSuccess)
                println(postError)
                cont.resume(Unit)
            }
        }
    }

    @Test
    fun analyze_invalidDates_postsError() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        val start = LocalDateTime.now().minusYears(999999999)
        val end = LocalDateTime.now().minusYears(999999999)

        suspendCoroutine<Unit> { cont ->
            Sahha.getScores(
                setOf(SahhaScoreType.activity),
                Pair<LocalDateTime, LocalDateTime>(start, end)
            ) { err, success ->
                Sahha.di.defaultScope.launch {
                    delay(1500)
                    Assert.assertEquals(true, err?.isNotEmpty())
                    println(err)
                    cont.resume(Unit)
                }
            }
        }
    }

    @Ignore("Inconsistent")
    @Test
    fun sensorData_invalidDates_postsError() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        saveInvalidSensorData()

        suspendCoroutine<Unit> { cont ->
            CoroutineScope(cont.context).launch {
                Sahha.sim.sensor.postSensorData(activity) { err, success ->
                    Sahha.di.defaultScope.launch {
                        delay(1500)
                        Assert.assertEquals(true, err?.isNotEmpty())
                        Assert.assertEquals(false, success)
                        println(err)
                        clearSensorData()
                        cont.resume(Unit)
                    }
                }
            }
        }
    }

    private suspend fun clearSensorData() {
        Sahha.di.sensorRepo.clearAllStepSessions()
        Sahha.di.sleepDao.clearSleepDto()
        Sahha.di.deviceUsageRepo.clearAllUsages()
    }

    private suspend fun saveInvalidSensorData() {
        clearSensorData()

        val tm = Sahha.di.timeManager
        val start = tm.localDateTimeToISO(LocalDateTime.now().minusYears(999999999))
        val end = tm.localDateTimeToISO(LocalDateTime.now().minusYears(999999999))

        Sahha.di.sensorRepo.saveStepSession(
            StepSession(
                100,
                start,
                end
            )
        )

        Sahha.di.sleepDao.saveSleepDto(
            SleepDto(
                12345,
                "", end
            )
        )

        Sahha.di.deviceUsageRepo.saveUsages(
            listOf(
                PhoneUsage(
                    true, true, end
                )
            )
        )
    }

    @Test
    fun invalidDemographic_postsErrorBody() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        suspendCoroutine<Unit> { cont ->
            val demo = SahhaDemographic(
                age = 999999999,
                gender = "invalid gender",
                country = "invalid country",
                birthCountry = "invalid birth country",
                ethnicity = "invalid ethnicity",
                occupation = "invalid occupation",
                industry = "invalid industry",
                incomeRange = "invalid income range",
                education = "invalid education",
                relationship = "invalid relationship",
                locale = "invalid locale",
                livingArrangement = "invalid living arrangement",
                birthDate = "invalid birth date"
            )

            val call = Sahha.di.api.patchDemographic(
                Sahha.di.authRepo.getToken() ?: "",
                demo
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Sahha.di.sahhaErrorLogger.api(
                        response,
                    )

                    Sahha.di.ioScope.launch {
                        delay(1500)
                        Assert.assertEquals(
                            false,
                            response.errorBody() == null
                        )
                        cont.resume(Unit)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
            })
        }
    }
}