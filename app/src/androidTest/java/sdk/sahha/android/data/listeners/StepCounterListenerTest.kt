package sdk.sahha.android.data.listeners

import junit.framework.TestCase
import org.junit.Test
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.steps.StepData

class StepCounterListenerTest : TestCase() {
    val stm = SahhaTimeManager()
    val listener = StepCounterListener()

    @Test
    fun test_stepCounterDuplicates() {
        val stepDataList = mutableListOf<StepData>()

        listener.checkStepCountDuplicate(1234, stepDataList.lastOrNull()?.count, stm.nowInISO())
            ?.also { stepDataList.add(it) }
        listener.checkStepCountDuplicate(1234, stepDataList.lastOrNull()?.count, stm.nowInISO())
            ?.also { stepDataList.add(it) }

        println("test_stepCounterDuplicates: $stepDataList")

        assertEquals(1, stepDataList.count())
    }

    @Test
    fun test_stepCounterSinglesAndDuplicates() {
        val stepDataList = mutableListOf<StepData>()

        listener.checkStepCountDuplicate(1234, stepDataList.lastOrNull()?.count, stm.nowInISO())
            ?.also { stepDataList.add(it) }
        listener.checkStepCountDuplicate(1434, stepDataList.lastOrNull()?.count, stm.nowInISO())
            ?.also { stepDataList.add(it) }
        listener.checkStepCountDuplicate(1634, stepDataList.lastOrNull()?.count, stm.nowInISO())
            ?.also { stepDataList.add(it) }
        listener.checkStepCountDuplicate(1834, stepDataList.lastOrNull()?.count, stm.nowInISO())
            ?.also { stepDataList.add(it) }
        listener.checkStepCountDuplicate(1834, stepDataList.lastOrNull()?.count, stm.nowInISO())
            ?.also { stepDataList.add(it) }
        listener.checkStepCountDuplicate(1934, stepDataList.lastOrNull()?.count, stm.nowInISO())
            ?.also { stepDataList.add(it) }

        println("test_stepCounterSinglesAndDuplicates: $stepDataList")

        assertEquals(5, stepDataList.count())
    }

    @Test
    fun test_stepCounterSingles() {
        val stepDataList = mutableListOf<StepData>()
        listener.checkStepCountDuplicate(1234, stepDataList.lastOrNull()?.count, stm.nowInISO())
            ?.also { stepDataList.add(it) }
        listener.checkStepCountDuplicate(1334, stepDataList.lastOrNull()?.count, stm.nowInISO())
            ?.also { stepDataList.add(it) }

        println("test_stepCounterSingles: $stepDataList")

        assertEquals(2, stepDataList.count())
    }

//    @Test
//    fun test_dbReturnsNullOnEmpty() = runBlocking {
//        val db = Room.inMemoryDatabaseBuilder(
//            ApplicationProvider.getApplicationContext(),
//            SahhaDatabase::class.java
//        ).build()
//        val movementDao = db.movementDao()
//
//        val lastStep = movementDao.getLastStepCount()
//
//        db.close()
//
//        assertEquals(null, lastStep)
//    }

//    @Test
//    fun test_dbDuplicates() = runBlocking {
//        val db = Room.inMemoryDatabaseBuilder(
//            ApplicationProvider.getApplicationContext(),
//            SahhaDatabase::class.java
//        ).build()
//        val movementDao = db.movementDao()
//
//        listener.checkStepCountDuplicate(1234, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//        listener.checkStepCountDuplicate(1234, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//
//        println("test_dbDuplicates: ${movementDao.getAllStepData()}")
//
//        assertEquals(1, movementDao.getAllStepData().count())
//        db.close()
//    }
//
//    @Test
//    fun test_dbSinglesAndDuplicates() = runBlocking {
//        val db = Room.inMemoryDatabaseBuilder(
//            ApplicationProvider.getApplicationContext(),
//            SahhaDatabase::class.java
//        ).build()
//        val movementDao = db.movementDao()
//
//        listener.checkStepCountDuplicate(1234, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//        listener.checkStepCountDuplicate(1434, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//        listener.checkStepCountDuplicate(1634, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//        listener.checkStepCountDuplicate(1834, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//        listener.checkStepCountDuplicate(1834, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//        listener.checkStepCountDuplicate(1934, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//
//        println("test_dbSinglesAndDuplicates: ${movementDao.getAllStepData()}")
//
//        assertEquals(5, movementDao.getAllStepData().count())
//        db.close()
//    }
//
//    @Test
//    fun test_dbSingles() = runBlocking {
//        val db = Room.inMemoryDatabaseBuilder(
//            ApplicationProvider.getApplicationContext(),
//            SahhaDatabase::class.java
//        ).build()
//        val movementDao = db.movementDao()
//
//        listener.checkStepCountDuplicate(1234, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//        listener.checkStepCountDuplicate(1434, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//
//        println("test_dbSingles: ${movementDao.getAllStepData()}")
//
//        assertEquals(2, movementDao.getAllStepData().count())
//        db.close()
//    }
//
//    @Test
//    fun test_dbDetectorAndCounter() = runBlocking {
//        val db = Room.inMemoryDatabaseBuilder(
//            ApplicationProvider.getApplicationContext(),
//            SahhaDatabase::class.java
//        ).build()
//        val movementDao = db.movementDao()
//
//        listener.checkStepCountDuplicate(1234, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//        movementDao.saveStepData(
//            StepData(
//                StepDataSource.AndroidStepDetector.name,
//                1,
//                stm.nowInISO()
//            )
//        )
//        movementDao.saveStepData(
//            StepData(
//                StepDataSource.AndroidStepDetector.name,
//                1,
//                stm.nowInISO()
//            )
//        )
//        movementDao.saveStepData(
//            StepData(
//                StepDataSource.AndroidStepDetector.name,
//                1,
//                stm.nowInISO()
//            )
//        )
//        listener.checkStepCountDuplicate(1434, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//        movementDao.saveStepData(
//            StepData(
//                StepDataSource.AndroidStepDetector.name,
//                1,
//                stm.nowInISO()
//            )
//        )
//        listener.checkStepCountDuplicate(1434, movementDao.getLastStepCount(), stm.nowInISO())
//            ?.also { movementDao.saveStepData(it) }
//
//        println("test_dbDetectorAndCounter: ${movementDao.getAllStepData()}")
//
//        assertEquals(6, movementDao.getAllStepData().count())
//        db.close()
//    }
}