package sdk.sahha.android.common

import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.health.connect.client.time.TimeRangeFilter
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Keep
internal class SahhaTimeManager {
    val zoneOffset: ZoneOffset get() = ZoneId.systemDefault().rules.getOffset(Instant.now())

    private val formatterPattern = "yyyy-MM-dd'T'HH:mm:ss.SSZZZZZ"
    private val simpleDateFormat = SimpleDateFormat(formatterPattern, Locale.US)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern(formatterPattern, Locale.US)

    fun nowInISO(): String {
        val now = ZonedDateTime.now(ZoneId.systemDefault()).withFixedOffsetZone()
        val nowInISO = now.format(dateTimeFormatter)
        return (nowInISO)
    }

    fun last24HoursInISO(): String {
        val last24Hours =
            ZonedDateTime.now(ZoneId.systemDefault()).withFixedOffsetZone().minusHours(24)
        val last24HoursISO = last24Hours.format(dateTimeFormatter)
        return (last24HoursISO)
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
                .format(dateTimeFormatter)
        return (iso)
    }

    fun localDateTimeToISO(
        localDateTime: LocalDateTime,
        zoneId: ZoneId? = ZoneId.systemDefault()
    ): String {
        val iso =
            localDateTime.atZone(zoneId)
                .format(dateTimeFormatter)
        return (iso)
    }

    @Deprecated("Date is outdated and formatting from it produces incorrect format")
    fun dateToISO(date: Date): String {
        return (simpleDateFormat.format(date))
    }

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
        return zdt.format(dateTimeFormatter)
    }

    fun epochMillisToISO(epochTimeMS: Long): String {
        val instant = Instant.ofEpochMilli(epochTimeMS)
        val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()).withFixedOffsetZone()
        return zdt.format(dateTimeFormatter)
    }

    fun epochMillisToZdt(epochMillis: Long): ZonedDateTime {
        val instant = Instant.ofEpochMilli(epochMillis)
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()).withFixedOffsetZone()
    }

    fun ISOToZonedDateTime(iso: String): ZonedDateTime {
        return ZonedDateTime.parse(iso, dateTimeFormatter)
            .withFixedOffsetZone()
    }

    fun convertNanosToMillis(nano: Long): Long {
        return nano / 1000000
    }

    fun getTimezone(): String {
        return ZonedDateTime.now().offset.toString()
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
            instant.atOffset(it).format(dateTimeFormatter)
        } ?: instant.atZone(ZonedDateTime.now().offset).format(dateTimeFormatter)
    }

    fun calculateDurationFromInstant(start: Instant, end: Instant): Int {
        return ((end.epochSecond - start.epochSecond) / 60).toInt()
    }
}

fun ZonedDateTime.toMidnight(plusDays: Long = 0): ZonedDateTime {
    return this
        .plusDays(plusDays)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
}

fun ZonedDateTime.toNoon(plusDays: Long = 0): ZonedDateTime {
    val zoneId = ZoneId.systemDefault()
    return ZonedDateTime.of(
        LocalDateTime.of(
            this.toLocalDate().plusDays(plusDays),
            LocalTime.NOON
        ),
        zoneId
    )
}

fun ZonedDateTime.toSpecificHour(
    hour: Int,
    plusDays: Long = 0
): ZonedDateTime {
    val zoneId = ZoneId.systemDefault()
    return ZonedDateTime.of(
        LocalDateTime.of(
            this.toLocalDate().plusDays(plusDays),
            LocalTime.of(hour, 0)
        ),
        zoneId
    )
}
