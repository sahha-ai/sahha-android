package sdk.sahha.android.domain.receiver

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase

class SleepReceiverTest : TestCase() {
    private lateinit var receiver: SleepReceiver
    private lateinit var context: Context

    override fun setUp() {
        receiver = SleepReceiver()
        context = ApplicationProvider.getApplicationContext()
    }

    fun test() {

    }
}