package com.example

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.data.models.BookingItem
import com.example.data.models.CheckInOutcome
import com.example.data.services.BookingReminderPlan
import com.example.data.services.BookingReminderPlanner
import com.example.data.services.GymOwnerRepository
import com.example.notifications.BookingReminderReceiver
import com.example.notifications.BookingReminderScheduler
import java.util.Calendar
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager

/**
 * GymOwnerRepository is shared in-memory state. The repository test here changes bookings
 * b1, b2 and b5; no other Robolectric test uses them (CheckInRulesTest only reads b1, and it
 * runs outside Robolectric with its own copy of the repository).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BookingReminderSchedulerTest {

  private val context = ApplicationProvider.getApplicationContext<Context>()
  private val alarms: ShadowAlarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))

  // Today at 10:00 AM in the default time zone, so "Today 06:00 PM" bookings are upcoming
  private val now = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 10)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }.timeInMillis
  private val scheduler = BookingReminderScheduler(context) { now }

  private fun booking(id: String, status: String = "Upcoming", time: String = "06:00 PM - 07:30 PM", shift: String = "Evening Shift") =
    BookingItem(id = id, customerName = "Member $id", membershipPlan = "Monthly Plan", date = "Today", time = time, shift = shift, status = status)

  private fun expectedTrigger(b: BookingItem) =
    (BookingReminderPlanner.plan(b, now) as BookingReminderPlan.Scheduled).triggerAtMillis

  private fun alarmFor(bookingId: String) =
    alarms.scheduledAlarms.singleOrNull { it.intent().data?.lastPathSegment == bookingId }

  // Robolectric 4.16 deprecates the field but has no replacement accessor for the PendingIntent
  @Suppress("DEPRECATION")
  private fun ShadowAlarmManager.ScheduledAlarm.intent(): Intent = shadowOf(operation).savedIntent

  @Before
  fun setUp() {
    BookingReminderScheduler.createChannel(context)
  }

  @After
  fun tearDown() {
    GymOwnerRepository.onBookingsChanged = null
  }

  @Test
  fun only_upcoming_future_bookings_get_an_alarm_30_minutes_before_start() {
    val upcoming = booking("u1")
    scheduler.sync(
      listOf(
        upcoming,
        booking("x_cancelled", status = "Cancelled"),
        booking("x_completed", status = "Completed"),
        booking("x_past", time = "08:00 AM - 09:00 AM")
      )
    )

    assertEquals(1, alarms.scheduledAlarms.size)
    val alarm = alarmFor("u1")!!
    assertEquals(expectedTrigger(upcoming), alarm.triggerAtMs)
    assertEquals(upcoming.time, alarm.intent().getStringExtra(BookingReminderScheduler.EXTRA_TIME))
  }

  @Test
  fun changing_time_or_shift_replaces_the_alarm_instead_of_adding_one() {
    scheduler.sync(booking("u2"))

    val moved = booking("u2", time = "07:00 PM - 08:00 PM", shift = "Night Shift")
    scheduler.sync(moved)

    assertEquals(1, alarms.scheduledAlarms.size)
    val alarm = alarmFor("u2")!!
    assertEquals(expectedTrigger(moved), alarm.triggerAtMs)
    assertEquals("Night Shift", alarm.intent().getStringExtra(BookingReminderScheduler.EXTRA_SHIFT))
  }

  @Test
  fun cancelling_or_completing_a_booking_removes_its_alarm() {
    scheduler.sync(listOf(booking("u3"), booking("u4")))
    assertEquals(2, alarms.scheduledAlarms.size)

    scheduler.sync(booking("u3", status = "Cancelled"))
    scheduler.sync(booking("u4", status = "Completed"))
    assertTrue(alarms.scheduledAlarms.isEmpty())
  }

  @Test
  fun repository_changes_keep_reminders_in_step() {
    GymOwnerRepository.onBookingsChanged = { scheduler.sync(it) }
    scheduler.sync(GymOwnerRepository.getBookings())
    // b5: Sneha Joseph, Tomorrow 06:30 AM; b2: Ananya Nair, Today 08:00 PM
    assertNotNull(alarmFor("b5"))
    assertNotNull(alarmFor("b2"))

    GymOwnerRepository.rescheduleBooking("b2", "Tomorrow", "07:00 AM - 08:30 AM")
    val b2 = GymOwnerRepository.getBookings().first { it.id == "b2" }
    assertEquals(expectedTrigger(b2), alarmFor("b2")!!.triggerAtMs)

    GymOwnerRepository.updateBookingShift("b2", "Morning Shift")
    assertEquals("Morning Shift", alarmFor("b2")!!.intent().getStringExtra(BookingReminderScheduler.EXTRA_SHIFT))

    GymOwnerRepository.cancelBooking("b5")
    assertEquals(null, alarmFor("b5"))

    // Checking in completes the booking, so its reminder goes too (b1: Rahul Menon, active member)
    assertNotNull(alarmFor("b1"))
    val arrival = GymOwnerRepository.markBookingArrived("b1")
    assertTrue(arrival.toString(), arrival is CheckInOutcome.Success)
    assertEquals(null, alarmFor("b1"))
  }

  @Test
  fun receiver_posts_a_staff_notification_when_permission_is_granted() {
    shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    scheduler.sync(booking("u5"))

    BookingReminderReceiver().onReceive(context, alarmFor("u5")!!.intent())

    val posted = shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications
    assertEquals(1, posted.size)
    val notification = posted.single()
    assertEquals(BookingReminderScheduler.CHANNEL_ID, notification.channelId)
    val text = notification.extras.getCharSequence("android.bigText").toString()
    assertTrue(text, text.contains("Member u5") && text.contains("Staff reminder on this device"))
  }

  @Test
  fun receiver_posts_nothing_without_notification_permission() {
    shadowOf(context as Application).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
    scheduler.sync(booking("u6"))

    BookingReminderReceiver().onReceive(context, alarmFor("u6")!!.intent())

    assertTrue(shadowOf(context.getSystemService(NotificationManager::class.java)).allNotifications.isEmpty())
  }

  @Test
  fun channel_is_created_for_booking_reminders() {
    val channel = context.getSystemService(NotificationManager::class.java).getNotificationChannel(BookingReminderScheduler.CHANNEL_ID)
    assertNotNull(channel)
    assertEquals("Booking reminders", channel.name)
  }
}
