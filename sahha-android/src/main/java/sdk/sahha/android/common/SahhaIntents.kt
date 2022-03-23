package sdk.sahha.android.common

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import sdk.sahha.android.Sahha

object SahhaIntents {
    fun settings(): Intent {
        val openSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val packageNameUri =
            Uri.fromParts("package", Sahha.di.activity.packageName, null)

        openSettingsIntent.data = packageNameUri

        return openSettingsIntent
    }
}