package sdk.sahha.android.domain.model.metadata

internal interface HasMetadata<T> {
    val postDateTimes: ArrayList<String>?
    val modifiedDateTime: String?
    fun copyWithMetadata(
        postDateTimes: ArrayList<String>? = null,
        modifiedDateTime: String? = null
    ): T
}