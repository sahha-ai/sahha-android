package sdk.sahha.android.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

internal object SahhaIntents {
    fun settings(context: Context): Intent {
        val openSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val packageNameUri =
            Uri.fromParts("package", context.packageName, null)

        openSettingsIntent.data = packageNameUri

        return openSettingsIntent
    }
}