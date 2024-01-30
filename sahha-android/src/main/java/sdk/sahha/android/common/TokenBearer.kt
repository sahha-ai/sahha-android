package sdk.sahha.android.common

internal object TokenBearer {
    operator fun invoke(token: String): String {
        return "Profile $token"
    }
}