package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.data.models.BookingItem
import com.example.data.services.BookingReminderPlan
import com.example.data.services.BookingReminderPlanner
import com.example.data.services.GymOwnerRepository

/**
 * Schedules local notifications on this device that remind staff about upcoming bookings.
 * Nothing is sent to members. [sync] is idempotent: each booking's alarm is replaced, or
 * cancelled when the booking no longer qualifies (cancelled, completed, or past).
 */
class BookingReminderScheduler(
    context: Context,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun sync(bookings: List<BookingItem>) {
        bookings.forEach { sync(it) }
    }

    fun sync(booking: BookingItem) {
        when (val plan = BookingReminderPlanner.plan(booking, clock())) {
            is BookingReminderPlan.Scheduled -> schedule(booking, plan.triggerAtMillis)
            is BookingReminderPlan.NotScheduled -> {
                cancel(booking.id)
                // A cancelled booking shouldn't leave a stale reminder in the shade
                if (booking.status.equals("Cancelled", ignoreCase = true)) {
                    NotificationManagerCompat.from(appContext).cancel(notificationId(booking.id))
                }
            }
        }
    }

    private fun schedule(booking: BookingItem, triggerAtMillis: Long) {
        val intent = reminderIntent(appContext, booking.id)
            .putExtra(EXTRA_CUSTOMER_NAME, booking.customerName)
            .putExtra(EXTRA_PLAN, booking.membershipPlan)
            .putExtra(EXTRA_SHIFT, booking.shift)
            .putExtra(EXTRA_DATE, booking.date)
            .putExtra(EXTRA_TIME, booking.time)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Exact timing needs the user-granted exact-alarm permission on Android 12+; without it
        // Android may deliver the reminder a few minutes late, which is fine for a heads-up.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(bookingId: String) {
        val pendingIntent = PendingIntent.getBroadcast(
            appContext, 0, reminderIntent(appContext, bookingId),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    companion object {
        const val CHANNEL_ID = "booking_reminders"
        const val EXTRA_BOOKING_ID = "booking_id"
        const val EXTRA_CUSTOMER_NAME = "customer_name"
        const val EXTRA_PLAN = "membership_plan"
        const val EXTRA_SHIFT = "shift"
        const val EXTRA_DATE = "date"
        const val EXTRA_TIME = "time"

        /** Creates the channel, schedules reminders for current bookings and keeps them in step with booking changes. */
        fun install(context: Context) {
            createChannel(context)
            val scheduler = BookingReminderScheduler(context)
            GymOwnerRepository.onBookingsChanged = { scheduler.sync(it) }
            scheduler.sync(GymOwnerRepository.getBookings())
        }

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val channel = NotificationChannel(CHANNEL_ID, "Booking reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Reminders for staff on this device, 30 minutes before a booked session. Members are not notified."
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        /** True when this device will actually show reminder notifications. */
        fun canPostNotifications(context: Context): Boolean {
            val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

        fun notificationId(bookingId: String): Int = bookingId.hashCode()

        // The data URI makes each booking's PendingIntent distinct, so it can be replaced or cancelled on its own
        internal fun reminderIntent(context: Context, bookingId: String): Intent =
            Intent(context, BookingReminderReceiver::class.java)
                .setData(Uri.parse("byce-booking-reminder://booking/${Uri.encode(bookingId)}"))
                .putExtra(EXTRA_BOOKING_ID, bookingId)
    }
}
