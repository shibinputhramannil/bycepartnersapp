package com.example.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.services.BookingReminderPlanner
import com.example.notifications.BookingReminderScheduler.Companion.EXTRA_BOOKING_ID
import com.example.notifications.BookingReminderScheduler.Companion.EXTRA_CUSTOMER_NAME
import com.example.notifications.BookingReminderScheduler.Companion.EXTRA_DATE
import com.example.notifications.BookingReminderScheduler.Companion.EXTRA_PLAN
import com.example.notifications.BookingReminderScheduler.Companion.EXTRA_SHIFT
import com.example.notifications.BookingReminderScheduler.Companion.EXTRA_TIME

/** Shows the staff reminder when a booking's alarm fires. */
class BookingReminderReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission") // checked by canPostNotifications
    override fun onReceive(context: Context, intent: Intent) {
        val bookingId = intent.getStringExtra(EXTRA_BOOKING_ID) ?: return
        if (!BookingReminderScheduler.canPostNotifications(context)) return
        BookingReminderScheduler.createChannel(context)

        val name = intent.getStringExtra(EXTRA_CUSTOMER_NAME).orEmpty()
        val plan = intent.getStringExtra(EXTRA_PLAN).orEmpty()
        val shift = intent.getStringExtra(EXTRA_SHIFT).orEmpty()
        val date = intent.getStringExtra(EXTRA_DATE).orEmpty()
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()
        val startTime = time.substringBefore("-").trim()

        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Lock screen shows no member names
        val publicVersion = NotificationCompat.Builder(context, BookingReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_booking_reminder)
            .setContentTitle("Booking reminder")
            .setContentText("A booked session starts in ${BookingReminderPlanner.MINUTES_BEFORE} minutes.")
            .build()

        val notification = NotificationCompat.Builder(context, BookingReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_booking_reminder)
            .setContentTitle("Booking at $startTime · $shift")
            .setContentText("$name · $plan")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$name · $plan\n$date · $time\nStaff reminder on this device. The member isn't notified."
                )
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(BookingReminderScheduler.notificationId(bookingId), notification)
    }
}
