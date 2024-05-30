package sdk.sahha.android.domain.use_case

import android.os.Build
import android.util.Log
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.model.error_log.CrashReason
import sdk.sahha.android.domain.repository.AppCrashRepo
import javax.inject.Inject

private const val TAG = "UploadLatestCrashLog"

internal class UploadLatestCrashLog @Inject constructor(
    private val appCrashRepo: AppCrashRepo,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val timeManager: SahhaTimeManager
) {
    operator fun invoke(packageName: String) {
        val log = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            appCrashRepo.getAppExitInfo(packageName).maxByOrNull { it.timestamp }
        else null

        log?.also {
            val noDescriptionFound = it.description.isEmpty()
            if (noDescriptionFound) {
                Log.d(
                    TAG,
                    "No valid crash log was found"
                )
                return
            }

            sahhaErrorLogger.application(
                message = "${CrashReason.fromReason(it.reason)?.name ?: Constants.UNKNOWN}: ${it.description} at ${
                    timeManager.epochMillisToISO(
                        it.timestamp
                    )
                }",
                method = "invoke",
                path = TAG,
            ) { error, successful ->
                error?.also { e -> Log.e(TAG, e) }
                if (successful) {
                    Log.d(
                        TAG,
                        "Successfully uploaded crash log:\n\n" +
                                "message: ${CrashReason.fromReason(it.reason)?.name ?: Constants.UNKNOWN}: ${it.description} at ${
                                    timeManager.epochMillisToISO(
                                        it.timestamp
                                    )
                                }\n" +
                                "method: invoke\n" +
                                "path: $TAG",
                    )
                }
            }
        }
    }
}