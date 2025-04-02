package sdk.sahha.android.data.listeners

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Test
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.framework.listeners.StepCounterListener

class StepCounterListenerTest : TestCase() {
    internal val stm = SahhaTimeManager()
    internal val listener = StepCounterListener()

    @Test
    fun test_dbReturnsNullOnEmpty() = runTest {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SahhaDatabase::class.java
        ).build()
        val movementDao = db.movementDao()

        val lastStep = movementDao.getExistingStepCount(1234)

        db.close()

        assertEquals(null, lastStep)
    }

    @Test
    fun test_dbDuplicates() = runTest {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SahhaDatabase::class.java
        ).build()
        val movementDao = db.movementDao()

        listener.checkStepCountDuplicate(1234, movementDao.getExistingStepCount(1234), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        listener.checkStepCountDuplicate(1234, movementDao.getExistingStepCount(1234), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }

        println("test_dbDuplicates: ${movementDao.getAllStepData()}")

        assertEquals(1, movementDao.getAllStepData().count())
        db.close()
    }

    @Test
    fun test_dbSinglesAndDuplicates() = runTest {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SahhaDatabase::class.java
        ).build()
        val movementDao = db.movementDao()

        listener.checkStepCountDuplicate(1234, movementDao.getExistingStepCount(1234), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        listener.checkStepCountDuplicate(1434, movementDao.getExistingStepCount(1434), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        listener.checkStepCountDuplicate(1634, movementDao.getExistingStepCount(1634), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        listener.checkStepCountDuplicate(1834, movementDao.getExistingStepCount(1834), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        listener.checkStepCountDuplicate(1834, movementDao.getExistingStepCount(1834), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        listener.checkStepCountDuplicate(1934, movementDao.getExistingStepCount(1934), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }

        println("test_dbSinglesAndDuplicates: ${movementDao.getAllStepData()}")

        assertEquals(5, movementDao.getAllStepData().count())
        db.close()
    }

    @Test
    fun test_dbSingles() = runTest {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SahhaDatabase::class.java
        ).build()
        val movementDao = db.movementDao()

        listener.checkStepCountDuplicate(1234, movementDao.getExistingStepCount(1234), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        listener.checkStepCountDuplicate(1434, movementDao.getExistingStepCount(1434), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }

        println("test_dbSingles: ${movementDao.getAllStepData()}")

        assertEquals(2, movementDao.getAllStepData().count())
        db.close()
    }

    @Test
    fun test_dbDetectorAndCounter() = runTest {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SahhaDatabase::class.java
        ).build()
        val movementDao = db.movementDao()

        listener.checkStepCountDuplicate(1234, movementDao.getExistingStepCount(1234), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        movementDao.saveStepData(
            StepData(
                Constants.STEP_DETECTOR_DATA_SOURCE,
                1,
                stm.nowInISO()
            )
        )
        movementDao.saveStepData(
            StepData(
                Constants.STEP_DETECTOR_DATA_SOURCE,
                1,
                stm.nowInISO()
            )
        )
        movementDao.saveStepData(
            StepData(
                Constants.STEP_DETECTOR_DATA_SOURCE,
                1,
                stm.nowInISO()
            )
        )
        listener.checkStepCountDuplicate(1434, movementDao.getExistingStepCount(1434), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        movementDao.saveStepData(
            StepData(
                Constants.STEP_DETECTOR_DATA_SOURCE,
                1,
                stm.nowInISO()
            )
        )
        listener.checkStepCountDuplicate(1434, movementDao.getExistingStepCount(1434), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }

        println("test_dbDetectorAndCounter: ${movementDao.getAllStepData()}")

        assertEquals(6, movementDao.getAllStepData().count())
        db.close()
    }

    @Test
    fun test_dbDuplicatesUnordered() = runTest {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SahhaDatabase::class.java
        ).build()
        val movementDao = db.movementDao()

        listener.checkStepCountDuplicate(1234, movementDao.getExistingStepCount(1234), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        listener.checkStepCountDuplicate(1334, movementDao.getExistingStepCount(1334), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }
        listener.checkStepCountDuplicate(1234, movementDao.getExistingStepCount(1234), stm.nowInISO())
            ?.also { movementDao.saveStepData(it) }

        assertEquals(2, movementDao.getAllStepData().count())
    }
}