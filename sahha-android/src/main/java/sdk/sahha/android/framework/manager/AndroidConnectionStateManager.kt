package sdk.sahha.android.framework.manager

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import sdk.sahha.android.domain.manager.ConnectionStateManager
import javax.inject.Inject

class AndroidConnectionStateManager @Inject constructor(
    private val context: Context
) : ConnectionStateManager {
    override fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}