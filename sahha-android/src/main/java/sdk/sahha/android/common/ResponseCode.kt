package sdk.sahha.android.common

internal object ResponseCode {
    fun isSuccessful(code: Int): Boolean {
        return code in 200..299
    }

    fun isUnauthorized(code: Int): Boolean {
        return code == 401
    }
}