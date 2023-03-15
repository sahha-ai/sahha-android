package sdk.sahha.android.common

import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.Constants.ONE_DAY_IN_MILLIS
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Keep
class SahhaTimeManager {
    private val formatterPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    private val simpleDateFormat =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(formatterPattern, Locale.getDefault())
        } else SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZZ", Locale.getDefault())

    fun nowInISO(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val now = ZonedDateTime.now(ZoneId.systemDefault()).withFixedOffsetZone()
            val nowInISO = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            return correctFormatting(nowInISO)
        } else {
            val now = Date()
            val nowInISO = simpleDateFormat.format(now)
            return correctFormatting(nowInISO)
        }
    }

    fun last24HoursInISO(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val last24Hours =
                ZonedDateTime.now(ZoneId.systemDefault()).withFixedOffsetZone().minusHours(24)
            val last24HoursISO = last24Hours.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            return correctFormatting(last24HoursISO)
        } else {
            val last24Hours = Date().time - ONE_DAY_IN_MILLIS
            val last24HoursISO = simpleDateFormat.format(last24Hours)
            return correctFormatting(last24HoursISO)
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
        return correctFormatting(iso)
    }

    fun dateToISO(date: Date): String {
        return correctFormatting(simpleDateFormat.format(date))
    }

    // Extra check for when there was a duplicate 'z' bug
    private fun correctFormatting(isoTime: String): String {
        if (isoTime[isoTime.lastIndex] == 'Z' && isoTime[isoTime.lastIndex - 1] == 'Z') {
            return isoTime.substring(0, isoTime.length - 1)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (isoTime[isoTime.lastIndex - 2] != ':')
                return StringBuilder(isoTime).insert(isoTime.lastIndex - 1, ':').toString()
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

    fun getEpochMillisFrom(days: Int): Long {
        return nowInEpoch() - (ONE_DAY_IN_MILLIS * days)
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
            return correctFormatting(simpleDateFormat.format(epochTimeMS))
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

    fun getTimezone(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ZonedDateTime.now().offset.toString()
        } else {
            val now = Date()
            val nowInISO = simpleDateFormat.format(now)
            correctFormatting(nowInISO).substring(23)
        }
    }

    fun getTimeRangeFilter(startEpochMillis: Long, endEpochMillis: Long): TimeRangeFilter {
        return TimeRangeFilter.between(
            Instant.ofEpochMilli(startEpochMillis),
            Instant.ofEpochMilli(endEpochMillis)
        )
    }

    fun instantToIsoTime(
        instant: Instant,
        offset: ZoneOffset? = null
    ): String {
        return offset?.let {
            instant.atOffset(it).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } ?: instant.atZone(ZonedDateTime.now().offset).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    fun calculateDurationFromInstant(start: Instant, end: Instant): Int {
        return ((end.epochSecond - start.epochSecond) / 60).toInt()
    }
}
