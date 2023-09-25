package sdk.sahha.android.domain.use_case.post

import android.content.Context
import androidx.health.connect.client.impl.converters.datatype.toDataTypeIdPairProto
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import sdk.sahha.android.data.local.dao.TestDataDao
import sdk.sahha.android.domain.model.test.TestDataLocal
import sdk.sahha.android.domain.model.test.TestDataPost
import sdk.sahha.android.domain.model.test.toTestDataPost
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import javax.inject.Inject

class PostHealthConnectDataUseCase @Inject constructor(
    private val context: Context,
    private val repo: HealthConnectRepo,
    private val testDao: TestDataDao
) {
    suspend operator fun invoke(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        queryAndPostHealthConnectData(callback)
    }

    private fun setNextAlarmTime(
        amountToAdd: Long,
        timeUnit: TemporalUnit
    ) {
        val nextTimeStampEpochMillis = Instant.now().plus(amountToAdd, timeUnit).toEpochMilli()

        Sahha.di.sahhaAlarmManager.setAlarm(
            context, nextTimeStampEpochMillis
        )
    }

    private suspend fun queryAndPostHealthConnectData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        val granted = repo.getGrantedPermissions()

        granted.forEach {
            when (it) {
                HealthPermission.getReadPermission(StepsRecord::class) -> {
                    repo.getCurrentDayRecords(StepsRecord::class)?.also { r ->
                        val local = testDao.getTestLocalData()

                        val queries = r.map { qr ->
                            TestDataLocal(
                                metaId = qr.metadata.id,
                                count = qr.count,
                                json = SahhaConverterUtility.convertToJsonString(qr),
                                dataType = HealthPermission.getReadPermission(StepsRecord::class),
                                lastModifiedTime = qr.metadata.lastModifiedTime.toString()
                            )
                        }

                        for (record in queries) {
                            val localMatch = local.find { l -> l.metaId == record.metaId }

                            if (localMatch == null) {
                                saveLocalAndPost(record)
                                continue
                            }
                            if (localMatch.lastModifiedTime == record.lastModifiedTime)
                                continue

                            // Modified time is different
                            saveLocalAndDiffPost(localMatch, record)
                        }
                    }
                }

                HealthPermission.getReadPermission(SleepSessionRecord::class) -> {
                    repo.getCurrentDayRecords(SleepSessionRecord::class)?.also { r ->
                        val local = testDao.getTestLocalData()

                        val queries = r.map { qr ->
                            TestDataLocal(
                                metaId = qr.metadata.id,
                                json = SahhaConverterUtility.convertToJsonString(qr),
                                dataType = HealthPermission.getReadPermission(SleepSessionRecord::class),
                                lastModifiedTime = qr.metadata.lastModifiedTime.toString()
                            )
                        }

                        for (record in queries) {
                            val localMatch = local.find { l -> l.metaId == record.metaId }

                            if (localMatch == null) {
                                saveLocalAndPost(record)
                                continue
                            }
                            if (localMatch.lastModifiedTime == record.lastModifiedTime)
                                continue

                            // Modified time is different
                            saveLocalAndPost(record)
                        }
                    }
                }

                HealthPermission.getReadPermission(SleepStageRecord::class) -> {}
                HealthPermission.getReadPermission(HeartRateRecord::class) -> {}
                HealthPermission.getReadPermission(RestingHeartRateRecord::class) -> {}
                HealthPermission.getReadPermission(BloodGlucoseRecord::class) -> {}
                HealthPermission.getReadPermission(BloodPressureRecord::class) -> {}
            }

        }
        setNextAlarmTime(10, ChronoUnit.MINUTES)
        callback(null, true)
    }

    private suspend fun saveLocalAndPost(record: TestDataLocal) {
        testDao.saveTestLocalData(record)
        testDao.saveTestPostData(record.toTestDataPost())
    }

    private suspend fun saveLocalAndDiffPost(local: TestDataLocal, newRecord: TestDataLocal) {
        testDao.saveTestLocalData(newRecord)
        testDao.saveTestPostData(
            TestDataPost(
                count = (newRecord.count!! - local.count!!),
                json = newRecord.json,
                dataType = newRecord.dataType,
                lastModifiedTime = newRecord.lastModifiedTime
            )
        )
    }
}