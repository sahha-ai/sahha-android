//package sdk.sahha.android.suite
//
//import androidx.activity.ComponentActivity
//import androidx.test.core.app.ActivityScenario
//import androidx.test.core.app.ApplicationProvider
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.BeforeClass
//import org.junit.Test
//import sdk.sahha.android.domain.internal_enum.AppEventEnum
//import sdk.sahha.android.domain.model.app_event.AppEvent
//import sdk.sahha.android.domain.model.processor.AppEventProcessor
//import sdk.sahha.android.framework.processor.AppEventProcessorImpl
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaConverterUtility
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSettings
//import java.time.ZonedDateTime
//
//class AppEventLinkingTest {
//    companion object {
//        @BeforeClass
//        @JvmStatic
//        fun before() {
//            val scenario = ActivityScenario.launch(ComponentActivity::class.java)
//            scenario.onActivity { activity ->
//                val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)
//                Sahha.configure(activity, settings)
//            }
//        }
//    }
//
//    private val processor: AppEventProcessor = AppEventProcessorImpl(
//        context = ApplicationProvider.getApplicationContext(),
//        mapper = Sahha.di.mapperDefaults.mapper,
//        manager = Sahha.di.mapperDefaults.idManager
//    )
//
//    private val now = ZonedDateTime.now()
//    private val start = now.minusMinutes(1)
//
//    private val createEvent = AppEvent(AppEventEnum.APP_CREATE, start)
//    private val destroyEvent = AppEvent(AppEventEnum.APP_DESTROY, now)
//
//    @Test
//    fun createEvent_returnsNullLog() = runTest {
//        val create = processor.process(createEvent)
//        Assert.assertEquals(null, create)
//    }
//
//
//    @Test
//    fun destroyEvent_linksToCreateEvent() = runTest {
//        processor.process(createEvent)
//        val destroy = processor.process(destroyEvent)
//        val pretty = SahhaConverterUtility.convertToJsonString(destroy)
//        println(pretty)
//
//        Assert.assertEquals(60.0, destroy!!.value, 0.0)
//    }
//}