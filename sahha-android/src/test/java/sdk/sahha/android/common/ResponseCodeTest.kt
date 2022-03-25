package sdk.sahha.android.common

import junit.framework.TestCase.assertEquals
import org.junit.Test

class ResponseCodeTest {
    @Test
    fun test() {
        var code = ResponseCode.isSuccessful(200)
        assertEquals(true, code)
        code = ResponseCode.isSuccessful(299)
        assertEquals(true, code)
        code = ResponseCode.isSuccessful(300)
        assertEquals(false, code)
        code = ResponseCode.isSuccessful(199)
        assertEquals(false, code)
    }
}