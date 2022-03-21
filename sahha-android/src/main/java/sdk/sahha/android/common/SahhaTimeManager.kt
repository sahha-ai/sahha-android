package sdk.sahha.android.common

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.N)
internal object SahhaTimeManager {
    private val tag by lazy { "SahhaTimeManager" }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nowInISO(): String {
        val now = ZonedDateTime.now(ZoneId.systemDefault()).withFixedOffsetZone()
        val nowInISO = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return removeDuplicateZ(nowInISO)
    }

    // Extra check for when there was a duplicate 'z' bug
    private fun removeDuplicateZ(isoTime: String): String {
        if (isoTime[isoTime.lastIndex] == 'Z' && isoTime[isoTime.lastIndex - 1] == 'Z') {
            return isoTime.substring(0, isoTime.length - 1)
        }
        return isoTime
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nowInEpoch(): Long {
        return ZonedDateTime.now().withFixedOffsetZone().toInstant().toEpochMilli()
    }

    fun epochFrom(time: Long, minusInterval: Int, intervalType: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        cal.set(intervalType, minusInterval)
        return cal.timeInMillis
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun epochMinsToISO(epochTimeMinutes: Long): String {
        val epochMillis = epochTimeMinutes * 1000 * 60
        val instant = Instant.ofEpochMilli(epochMillis)
        val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        return zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun epochMillisToISO(epochTimeMS: Long): String {
        val instant = Instant.ofEpochMilli(epochTimeMS)
        val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()).withFixedOffsetZone()
        return zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun ISOToDate(iso: String): ZonedDateTime {
        return ZonedDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .withFixedOffsetZone()
    }

    fun convertNanosToMillis(nano: Long): Long {
        return nano / 1000000
    }
}