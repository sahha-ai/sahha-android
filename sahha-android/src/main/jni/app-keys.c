#include <jni.h>

JNIEXPORT jstring JNICALL
Java_sdk_sahha_android_di_AppModule_getApiUrlDev(JNIEnv *env, jobject instance) {
    return (*env)->  NewStringUTF(env, "https://sandbox-api.sahha.ai/api/");
}

JNIEXPORT jstring JNICALL
Java_sdk_sahha_android_di_AppModule_getApiUrlProd(JNIEnv *env, jobject instance) {
    return (*env)->  NewStringUTF(env, "https://api.sahha.ai/api/");
}

JNIEXPORT jstring JNICALL
Java_sdk_sahha_android_Sahha_getAppCenterDevKey(JNIEnv *env, jobject instance) {
    return (*env)->  NewStringUTF(env, "cd6df1cd-418d-4794-9170-5640e2377f56");
}

JNIEXPORT jstring JNICALL
Java_sdk_sahha_android_Sahha_getAppCenterProdKey(JNIEnv *env, jobject instance) {
    return (*env)->  NewStringUTF(env, "9c9fcabb-45f2-49d0-942a-b40481bc0863");
}