package com.example

import com.example.data.models.BookingItem
import com.example.data.services.BookingReminderPlan
import com.example.data.services.BookingReminderPlanner
import com.example.data.services.NoReminderReason
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class BookingReminderPlannerTest {

  private val zone = TimeZone.getTimeZone("Asia/Kolkata")

  private fun millis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
    Calendar.getInstance(zone).apply {
      clear()
      set(year, month, day, hour, minute, 0)
    }.timeInMillis

  // 26 Oct 2026, 12:00 PM
  private val now = millis(2026, Calendar.OCTOBER, 26, 12, 0)

  private fun booking(date: String = "Today", time: String = "06:00 PM - 07:30 PM", status: String = "Upcoming") =
    BookingItem(id = "t1", customerName = "Test Member", membershipPlan = "Monthly Plan", date = date, time = time, status = status)

  private fun plan(b: BookingItem) = BookingReminderPlanner.plan(b, now, zone)

  @Test
  fun upcoming_booking_today_fires_30_minutes_before_start() {
    assertEquals(
      BookingReminderPlan.Scheduled(
        triggerAtMillis = millis(2026, Calendar.OCTOBER, 26, 17, 30),
        sessionStartMillis = millis(2026, Calendar.OCTOBER, 26, 18, 0)
      ),
      plan(booking())
    )
  }

  @Test
  fun tomorrow_and_explicit_dates_are_resolved() {
    val tomorrow = plan(booking(date = "Tomorrow", time = "06:30 AM - 08:00 AM")) as BookingReminderPlan.Scheduled
    assertEquals(millis(2026, Calendar.OCTOBER, 27, 6, 0), tomorrow.triggerAtMillis)

    val dated = plan(booking(date = "28 Oct 2026", time = "08:00 PM - 09:30 PM")) as BookingReminderPlan.Scheduled
    assertEquals(millis(2026, Calendar.OCTOBER, 28, 19, 30), dated.triggerAtMillis)
  }

  @Test
  fun cancelled_and_completed_bookings_get_no_reminder() {
    assertEquals(BookingReminderPlan.NotScheduled(NoReminderReason.CANCELLED), plan(booking(status = "Cancelled")))
    assertEquals(BookingReminderPlan.NotScheduled(NoReminderReason.COMPLETED), plan(booking(status = "Completed")))
  }

  @Test
  fun past_bookings_get_no_reminder() {
    // Already started
    assertEquals(BookingReminderPlan.NotScheduled(NoReminderReason.PAST), plan(booking(time = "06:00 AM - 07:30 AM")))
    // Starts in 20 minutes, so the 30-minute reminder time has passed
    assertEquals(BookingReminderPlan.NotScheduled(NoReminderReason.PAST), plan(booking(time = "12:20 PM - 01:30 PM")))
    // Earlier date
    assertEquals(BookingReminderPlan.NotScheduled(NoReminderReason.PAST), plan(booking(date = "20 Oct 2026")))
  }

  @Test
  fun reminder_exactly_at_now_is_not_scheduled_but_one_minute_later_is() {
    assertEquals(BookingReminderPlan.NotScheduled(NoReminderReason.PAST), plan(booking(time = "12:30 PM - 01:30 PM")))
    assertEquals(
      millis(2026, Calendar.OCTOBER, 26, 12, 1),
      (plan(booking(time = "12:31 PM - 01:30 PM")) as BookingReminderPlan.Scheduled).triggerAtMillis
    )
  }

  @Test
  fun unreadable_date_or_time_gets_no_reminder() {
    assertEquals(BookingReminderPlan.NotScheduled(NoReminderReason.UNKNOWN_TIME), plan(booking(time = "Evening")))
    assertEquals(BookingReminderPlan.NotScheduled(NoReminderReason.UNKNOWN_TIME), plan(booking(date = "Someday")))
  }
}
