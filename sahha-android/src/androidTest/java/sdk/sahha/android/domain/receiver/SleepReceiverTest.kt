package sdk.sahha.android.domain.receiver

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.location.SleepSegmentEvent
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.source.SahhaSettings
import sdk.sahha.android.source.SahhaEnvironment

class SleepReceiverTest : TestCase() {
    private lateinit var receiver: SleepReceiver
    private lateinit var sleepDao: SleepDao

    class TestActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
            super.onCreate(savedInstanceState, persistentState)
            Sahha.configure(this, SahhaSettings(environment = SahhaEnvironment.DEVELOPMENT))
        }
    }

    override fun setUp() {
        receiver = SleepReceiver()
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            sleepDao = Sahha.di.sleepDao
        }
    }

    fun test() {
        val intent = Intent().putExtra("sleep", SleepSegmentEvent(1L, 1L, 1, 1, 1))
        receiver.onReceive(ApplicationProvider.getApplicationContext(), intent)

        runBlocking {
            val qCount = sleepDao.getSleepQueue().count()
            val hqCount = sleepDao.getSleepQueueHistory().count()

            assertEquals(1, qCount)
            assertEquals(1, hqCount)
        }
    }
}