package sdk.sahha.android.domain.model.metadata

internal interface HasMetadata<T> {
    val metadata: SahhaMetadata?
    fun copyWithMetadata(metadata: SahhaMetadata): T
}