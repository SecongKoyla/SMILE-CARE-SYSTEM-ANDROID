package com.android.smilecare.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility helpers for converting to/from an integer date key in the form YYYYMMDD.
 */
object ClinicDateUtils {

    fun ymdFromLocalCalendar(cal: Calendar): Int {
        val year = cal.get(Calendar.YEAR)
        val month1 = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return (year * 10000) + (month1 * 100) + day
    }

    fun ymdFromUtcMillis(utcMillis: Long): Int {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCal.timeInMillis = utcMillis
        val year = utcCal.get(Calendar.YEAR)
        val month1 = utcCal.get(Calendar.MONTH) + 1
        val day = utcCal.get(Calendar.DAY_OF_MONTH)
        return (year * 10000) + (month1 * 100) + day
    }

    fun dateFromYmdLocal(ymd: Int): Date {
        val year = ymd / 10000
        val month1 = (ymd / 100) % 100
        val day = ymd % 100

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month1 - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.time
    }

    fun formatYmd(ymd: Int, locale: Locale = Locale.getDefault()): String {
        val fmt = SimpleDateFormat("MMM dd, yyyy", locale)
        return fmt.format(dateFromYmdLocal(ymd))
    }
}
