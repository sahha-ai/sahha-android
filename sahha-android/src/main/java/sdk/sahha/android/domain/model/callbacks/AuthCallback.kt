package sdk.sahha.android.domain.model.callbacks

internal class AuthCallback {
    var authenticate: ((value: String) -> Unit)? = null
}