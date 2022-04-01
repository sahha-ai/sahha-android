package sdk.sahha.android.data.repository

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import sdk.sahha.android.Sahha
import sdk.sahha.android.common.TestUser.CUST_ID
import sdk.sahha.android.common.TestUser.PROFILE_ID
import sdk.sahha.android.data.remote.dto.SleepDto
import sdk.sahha.android.domain.model.config.SahhaSettings
import sdk.sahha.android.domain.model.enums.SahhaEnvironment
import sdk.sahha.android.domain.receiver.SleepReceiverTest
import sdk.sahha.android.domain.repository.RemotePostRepo

class RemotePostRepoImplTest : TestCase() {
    lateinit var repo: RemotePostRepo

    class TestActivity : ComponentActivity()

    override fun setUp() {
        super.setUp()
        ActivityScenario.launch(TestActivity::class.java).onActivity {
            Sahha.configure(it, SahhaSettings(environment = SahhaEnvironment.DEVELOPMENT))
            repo = Sahha.di.remotePostWorker
            Sahha.authenticate(CUST_ID, PROFILE_ID) {}
        }
    }

    @Test
    fun test() {
        runBlocking {
            Sahha.di.sleepDao.saveSleepDto(
                SleepDto(
                    360,
                    Sahha.timeManager.epochMillisToISO(1000000),
                    Sahha.timeManager.epochMillisToISO(1000000),
                    Sahha.timeManager.epochMillisToISO(1000000),
                )
            )

            assertEquals(1, Sahha.di.sleepDao.getSleepDto().count())

            repo.postSleepData { error, success ->
                error?.also { println(error) }
                success?.also { println(success) }

                runBlocking {
                    assertEquals(0, Sahha.di.sleepDao.getSleepDto().count())
                }
            }
        }
    }
}