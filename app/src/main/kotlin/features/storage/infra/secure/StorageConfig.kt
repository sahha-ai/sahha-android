package features.storage.infra.secure

/** Constants for SecureStoreAdapter. */
object StorageConfig {
    const val KEY_ALIAS = "sahha_sdk_secure_key"
    const val SECURE_FILE = "sahha_secure_store"


    const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    const val GCM_TAG_BITS = 128


    const val SUFFIX_IV = "::iv"
    const val SUFFIX_CT = "::ct"
}