package sdk.sahha.android.domain.model.callbacks

class AuthCallback {
    var authenticate: ((value: String) -> Unit)? = null
}