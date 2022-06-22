package sdk.sahha.android.common

import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import sdk.sahha.android.data.Constants.ONE_DAY_IN_MILLIS
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Keep
@RequiresApi(Build.VERSION_CODES.N)
class SahhaTimeManager {
    private val simpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())

    fun nowInISO(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val now = ZonedDateTime.now(ZoneId.systemDefault()).withFixedOffsetZone()
            val nowInISO = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            return removeDuplicateZ(nowInISO)
        } else {
            val now = Date()
            val nowInISO = simpleDateFormat.format(now)
            return removeDuplicateZ(nowInISO)
        }
    }

    fun last24HoursInISO(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val last24Hours =
                ZonedDateTime.now(ZoneId.systemDefault()).withFixedOffsetZone().minusHours(24)
            val last24HoursISO = last24Hours.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            return removeDuplicateZ(last24HoursISO)
        } else {
            val last24Hours = Date().time - ONE_DAY_IN_MILLIS
            val last24HoursISO = simpleDateFormat.format(last24Hours)
            return removeDuplicateZ(last24HoursISO)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun ISOtoEpoch(isoTime: String): Long {
        val dateTime = ZonedDateTime.parse(isoTime)

        return dateTime
            .toInstant()
            .toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun localDateTimeToISO(localDateTime: LocalDateTime): String {
        val iso =
            localDateTime.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return removeDuplicateZ(iso)
    }

    fun dateToISO(date: Date): String {
        return removeDuplicateZ(simpleDateFormat.format(date))
    }

    // Extra check for when there was a duplicate 'z' bug
    private fun removeDuplicateZ(isoTime: String): String {
        if (isoTime[isoTime.lastIndex] == 'Z' && isoTime[isoTime.lastIndex - 1] == 'Z') {
            return isoTime.substring(0, isoTime.length - 1)
        }
        return isoTime
    }

    fun nowInEpoch(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ZonedDateTime.now().withFixedOffsetZone().toInstant().toEpochMilli()
        } else {
            Date().time
        }
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

    fun epochMillisToISO(epochTimeMS: Long): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val instant = Instant.ofEpochMilli(epochTimeMS)
            val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()).withFixedOffsetZone()
            return zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } else {
            return removeDuplicateZ(simpleDateFormat.format(epochTimeMS))
        }
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
