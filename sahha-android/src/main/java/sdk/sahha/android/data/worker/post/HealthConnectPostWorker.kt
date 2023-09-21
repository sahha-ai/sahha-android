package sdk.sahha.android.data.worker.post

import android.content.Context
import androidx.health.connect.client.records.StepsRecord
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.use_case.query.QueryHealthConnectRecordsUseCase
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility

class HealthConnectPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        val queryUseCase = QueryHealthConnectRecordsUseCase(Sahha.di.healthConnectRepo)
        val records = queryUseCase(StepsRecord::class)
        val json = SahhaConverterUtility.convertToJsonString(records)
        Sahha.di.deviceUsageDao.saveUsage(
            PhoneUsage(false, false, json)
        )
        println(json)

        return Result.success()
    }
}