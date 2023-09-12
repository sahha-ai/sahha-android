package sdk.sahha.android.common

import android.content.Context
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.source.SahhaEnvironment

object Session {
    internal fun shouldBeDevEnvironment(
        context: Context,
        environment: Enum<SahhaEnvironment>
    ): Boolean {
        return BuildConfig.DEBUG &&
                environment == SahhaEnvironment.sandbox &&
                packageNameContains(
                    context, "sahha"
                )
    }

    private fun packageNameContains(context: Context, text: String): Boolean {
        val packageName = context.packageName

        return packageName
            .contains(text)
    }
}