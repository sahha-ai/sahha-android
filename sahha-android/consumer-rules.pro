-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken {
    <fields>;
    <methods>;
}
-keep class sdk.sahha.android.data.local.converter.** { *; }
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation