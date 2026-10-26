package com.example.data.services

import com.example.data.models.BookingItem
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** Whether a booking gets a staff reminder on this device, and when. */
sealed interface BookingReminderPlan {
    data class Scheduled(val triggerAtMillis: Long, val sessionStartMillis: Long) : BookingReminderPlan
    data class NotScheduled(val reason: NoReminderReason) : BookingReminderPlan
}

enum class NoReminderReason {
    CANCELLED,
    COMPLETED,
    /** The session has started or the reminder time has already passed. */
    PAST,
    /** The booking's date or time couldn't be read. */
    UNKNOWN_TIME
}

/**
 * Decides when a booking's local staff reminder fires: [MINUTES_BEFORE] minutes before the
 * session starts. Pure logic so it can be tested without Android.
 */
object BookingReminderPlanner {
    const val MINUTES_BEFORE = 30

    fun plan(booking: BookingItem, nowMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): BookingReminderPlan {
        if (booking.status.equals("Cancelled", ignoreCase = true)) {
            return BookingReminderPlan.NotScheduled(NoReminderReason.CANCELLED)
        }
        if (booking.status.equals("Completed", ignoreCase = true)) {
            return BookingReminderPlan.NotScheduled(NoReminderReason.COMPLETED)
        }
        val start = sessionStartMillis(booking.date, booking.time, nowMillis, timeZone)
            ?: return BookingReminderPlan.NotScheduled(NoReminderReason.UNKNOWN_TIME)
        val trigger = start - MINUTES_BEFORE * 60_000L
        return if (trigger <= nowMillis) {
            BookingReminderPlan.NotScheduled(NoReminderReason.PAST)
        } else {
            BookingReminderPlan.Scheduled(triggerAtMillis = trigger, sessionStartMillis = start)
        }
    }

    /**
     * Start of the session in epoch millis. [date] is "Today", "Tomorrow" or "dd MMM yyyy";
     * [time] is a slot window such as "06:00 PM - 07:30 PM". Returns null if either can't be read.
     */
    fun sessionStartMillis(date: String, time: String, nowMillis: Long, timeZone: TimeZone): Long? {
        val day = Calendar.getInstance(timeZone, Locale.US).apply { timeInMillis = nowMillis }
        when {
            date.equals("Today", ignoreCase = true) -> Unit
            date.equals("Tomorrow", ignoreCase = true) -> day.add(Calendar.DAY_OF_MONTH, 1)
            else -> day.time = parse("dd MMM yyyy", date.trim(), timeZone) ?: return null
        }

        val startText = time.substringBefore("-").trim()
        val startTime = Calendar.getInstance(timeZone, Locale.US).apply {
            this.time = parse("hh:mm a", startText, timeZone) ?: return null
        }

        day.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY))
        day.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE))
        day.set(Calendar.SECOND, 0)
        day.set(Calendar.MILLISECOND, 0)
        return day.timeInMillis
    }

    private fun parse(pattern: String, text: String, timeZone: TimeZone) = try {
        SimpleDateFormat(pattern, Locale.US).apply {
            isLenient = false
            this.timeZone = timeZone
        }.parse(text)
    } catch (_: ParseException) {
        null
    }
}
