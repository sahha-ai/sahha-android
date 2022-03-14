package sdk.sahha.android._refactor.common.security

import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.assertEquals
import org.junit.Test
import sdk.sahha.android._refactor.data.local.dao.SecurityDao
import sdk.sahha.android.data.Constants.UET
import javax.inject.Inject

class DecryptorTest @Inject constructor(
    securityDao: SecurityDao
) {
    private val encryptor = Encryptor(ApplicationProvider.getApplicationContext())
    private val decryptor = Decryptor(securityDao)

    @Test
    suspend fun decryptorResultsMatch() {
        encryptor.encryptText(UET, "test token")
        var result = decryptor.decryptToken()

        assertEquals("test token", result)

        encryptor.encryptText(UET, "test token two")
        result = decryptor.decryptToken()

        assertEquals("test token two", result)
    }
}