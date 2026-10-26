package com.example.ui.owner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.data.models.BookingItem
import com.example.data.models.CheckInCheck
import com.example.data.models.CheckInEligibility
import com.example.data.models.CheckInOutcome
import com.example.data.services.BookingReminderPlan
import com.example.data.services.BookingReminderPlanner
import com.example.data.services.GymOwnerRepository
import com.example.data.services.NoReminderReason
import com.example.notifications.BookingReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun OwnerBookingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedShiftFilter by remember { mutableStateOf("All Shifts") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("CSV") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val bookings = remember { mutableStateListOf<BookingItem>().apply { addAll(GymOwnerRepository.getBookings()) } }
    var selectedBookingForDetail by remember { mutableStateOf<BookingItem?>(null) }
    var bookingPendingCheckIn by remember { mutableStateOf<Pair<BookingItem, CheckInCheck>?>(null) }
    val shiftSchedules = remember(bookings.toList()) { GymOwnerRepository.getShiftSchedules() }
    // Reference time for reminder labels; refreshed whenever bookings change
    val nowMillis = remember(bookings.toList()) { System.currentTimeMillis() }

    var notificationsAllowed by remember { mutableStateOf(BookingReminderScheduler.canPostNotifications(context)) }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsAllowed = BookingReminderScheduler.canPostNotifications(context)
        permissionDenied = !granted
    }
    // Picks up changes made in system settings
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationsAllowed = BookingReminderScheduler.canPostNotifications(context)
    }

    fun enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionDenied) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            openNotificationSettings(context)
        }
    }

    fun completeArrival(booking: BookingItem, reason: String?) {
        when (val outcome = GymOwnerRepository.markBookingArrived(booking.id, reason)) {
            is CheckInOutcome.Success -> {
                val index = bookings.indexOfFirst { it.id == booking.id }
                if (index != -1) {
                    bookings[index] = booking.copy(arrivalStatus = "Checked In", status = "Completed")
                }
                statusMessage = "✓ ${booking.customerName} marked as Arrived & Checked In for ${booking.time} slot."
            }
            is CheckInOutcome.Rejected -> statusMessage = "⚠️ ${outcome.message}"
        }
    }

    // Active members check in straight away; others need the reason dialog; hard blocks only show a message
    fun requestArrival(booking: BookingItem) {
        val check = GymOwnerRepository.evaluateBookingCheckIn(booking.id)
        when (check.eligibility) {
            CheckInEligibility.ALLOWED -> completeArrival(booking, null)
            CheckInEligibility.BLOCKED -> statusMessage = "⚠️ ${check.message}"
            else -> bookingPendingCheckIn = booking to check
        }
    }

    val filteredBookings = remember(searchQuery, selectedFilter, selectedShiftFilter, bookings.toList()) {
        bookings.filter { item ->
            val matchesQuery = item.customerName.contains(searchQuery, ignoreCase = true) ||
                    item.membershipPlan.contains(searchQuery, ignoreCase = true) ||
                    item.customerEmail.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Today" -> item.date.equals("Today", ignoreCase = true)
                "Upcoming" -> item.status.equals("Upcoming", ignoreCase = true)
                "Completed" -> item.status.equals("Completed", ignoreCase = true)
                "Cancelled" -> item.status.equals("Cancelled", ignoreCase = true)
                else -> true
            }
            val matchesShift = when (selectedShiftFilter) {
                "Morning Shift" -> item.shift.contains("Morning", ignoreCase = true)
                "Evening Shift" -> item.shift.contains("Evening", ignoreCase = true)
                "Night Shift" -> item.shift.contains("Night", ignoreCase = true)
                else -> true
            }
            matchesQuery && matchesFilter && matchesShift
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CHECK-IN RESERVATIONS & SHIFTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextSubtle,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Slot Bookings & Reminders",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    // Export Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x20FFFFFF))
                            .clickable { showExportDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export", tint = TextWhite, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Export", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                statusMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusActiveGreenBg)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = msg, fontSize = 12.sp, color = StatusActiveGreen, fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = { statusMessage = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = StatusActiveGreen, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // -------------------------------------------------------------------
                // STAFF REMINDERS ON THIS DEVICE + SHIFT SUMMARY
                // -------------------------------------------------------------------
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(StatusInfoBlueBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = StatusInfoBlue,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "STAFF REMINDERS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextSubtle,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "Alerts on this device",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (notificationsAllowed) StatusActiveGreenBg else StatusPendingAmberBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (notificationsAllowed) "On" else "Off",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (notificationsAllowed) StatusActiveGreen else StatusPendingAmber
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "This phone shows a notification ${BookingReminderPlanner.MINUTES_BEFORE} minutes before each upcoming booking. It's a reminder for staff on this device only; members aren't notified.",
                            fontSize = 11.sp,
                            color = ByceCoolGray,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bookings are sample data and reset when the app restarts, so reminders only cover these sample bookings for now.",
                            fontSize = 11.sp,
                            color = ByceCoolGray,
                            lineHeight = 15.sp
                        )

                        if (!notificationsAllowed) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Notifications are off for BYCE Partner, so reminders won't appear.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusPendingAmber,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x20FFFFFF))
                                    .clickable { enableNotifications() }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionDenied) {
                                        "Allow notifications"
                                    } else {
                                        "Open notification settings"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Shift schedule cards: Morning, Evening, Night
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShiftScheduleMiniCard(
                                title = "Morning",
                                icon = "🌅",
                                gymStart = "06:00 AM",
                                count = shiftSchedules.firstOrNull { it.shiftName.contains("Morning") }?.activeBookingsCount ?: 0,
                                accentColor = Color(0xFFF59E0B),
                                isSelected = selectedShiftFilter == "Morning Shift",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedShiftFilter = if (selectedShiftFilter == "Morning Shift") "All Shifts" else "Morning Shift"
                                }
                            )

                            ShiftScheduleMiniCard(
                                title = "Evening",
                                icon = "🌇",
                                gymStart = "04:00 PM",
                                count = shiftSchedules.firstOrNull { it.shiftName.contains("Evening") }?.activeBookingsCount ?: 0,
                                accentColor = Color(0xFFF97316),
                                isSelected = selectedShiftFilter == "Evening Shift",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedShiftFilter = if (selectedShiftFilter == "Evening Shift") "All Shifts" else "Evening Shift"
                                }
                            )

                            ShiftScheduleMiniCard(
                                title = "Night",
                                icon = "🌙",
                                gymStart = "08:00 PM",
                                count = shiftSchedules.firstOrNull { it.shiftName.contains("Night") }?.activeBookingsCount ?: 0,
                                accentColor = Color(0xFF818CF8),
                                isSelected = selectedShiftFilter == "Night Shift",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedShiftFilter = if (selectedShiftFilter == "Night Shift") "All Shifts" else "Night Shift"
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Shift Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All Shifts", "Morning Shift", "Evening Shift", "Night Shift").forEach { shift ->
                        val isSelected = selectedShiftFilter == shift
                        val (icon, color) = when (shift) {
                            "Morning Shift" -> Pair("🌅", Color(0xFFF59E0B))
                            "Evening Shift" -> Pair("🌇", Color(0xFFF97316))
                            "Night Shift" -> Pair("🌙", Color(0xFF818CF8))
                            else -> Pair("⚡", TextWhite)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) color.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                                .clickable { selectedShiftFilter = shift }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "$icon $shift",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) color else TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OwnerSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search member or email...",
                    filterOptions = listOf("All", "Today", "Upcoming", "Completed", "Cancelled"),
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Showing ${filteredBookings.size} bookings in $selectedShiftFilter",
                    fontSize = 12.sp,
                    color = ByceCoolGray
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            if (filteredBookings.isEmpty()) {
                item {
                    OwnerEmptyState(
                        title = "No Bookings",
                        description = "No reservations match '$selectedFilter' in '$selectedShiftFilter'."
                    )
                }
            } else {
                items(filteredBookings, key = { it.id }) { booking ->
                    BookingRowCard(
                        booking = booking,
                        reminder = BookingReminderPlanner.plan(booking, nowMillis),
                        nowMillis = nowMillis,
                        onViewClick = { selectedBookingForDetail = booking },
                        onCancelClick = {
                            val index = bookings.indexOfFirst { it.id == booking.id }
                            if (index != -1) {
                                bookings[index] = booking.copy(status = "Cancelled", arrivalStatus = "Not Arrived")
                                GymOwnerRepository.cancelBooking(booking.id)
                            }
                        },
                        onMarkArrived = { requestArrival(booking) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        selectedBookingForDetail?.let { booking ->
            BookingDetailModal(
                booking = booking,
                reminder = BookingReminderPlanner.plan(booking, nowMillis),
                nowMillis = nowMillis,
                notificationsAllowed = notificationsAllowed,
                onDismiss = { selectedBookingForDetail = null },
                onUpdateShift = { newShift ->
                    val index = bookings.indexOfFirst { it.id == booking.id }
                    if (index != -1) {
                        val updated = booking.copy(shift = newShift)
                        bookings[index] = updated
                        selectedBookingForDetail = updated
                        // The repository change also reschedules this booking's staff reminder
                        GymOwnerRepository.updateBookingShift(booking.id, newShift)
                        statusMessage = "✓ Shift updated to $newShift for ${booking.customerName}."
                    }
                },
                onCancelBooking = {
                    val index = bookings.indexOfFirst { it.id == booking.id }
                    if (index != -1) {
                        bookings[index] = booking.copy(status = "Cancelled", arrivalStatus = "Not Arrived")
                        GymOwnerRepository.cancelBooking(booking.id)
                    }
                    selectedBookingForDetail = null
                },
                onMarkArrived = {
                    selectedBookingForDetail = null
                    requestArrival(booking)
                }
            )
        }

        bookingPendingCheckIn?.let { (booking, check) ->
            CheckInGateDialog(
                title = "Check In ${booking.customerName}",
                check = check,
                onDismiss = { bookingPendingCheckIn = null },
                onConfirm = { reason ->
                    bookingPendingCheckIn = null
                    completeArrival(booking, reason)
                }
            )
        }

        if (showExportDialog) {
            ExportModalDialog(
                moduleName = "Slot Bookings",
                selectedFormat = exportFormat,
                onFormatChange = { exportFormat = it },
                onDismiss = { showExportDialog = false }
            )
        }
    }
}

@Composable
private fun ShiftScheduleMiniCard(
    title: String,
    icon: String,
    gymStart: String,
    count: Int,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0x18FFFFFF))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$icon $title", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(text = "$count", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accentColor)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Start: $gymStart", fontSize = 10.sp, color = ByceCoolGray)
        }
    }
}

// Android 8+ has a per-app notification settings page; older versions use the app info page
private fun openNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    }
    try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        // No settings screen available; the card keeps showing that notifications are off
    }
}

// "05:30 PM" for today, "Tomorrow 06:00 AM" or "12 Oct, 06:00 AM" otherwise
private fun formatReminderTime(triggerAtMillis: Long, nowMillis: Long): String {
    val time = SimpleDateFormat("hh:mm a", Locale.US).format(Date(triggerAtMillis))
    val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val trigger = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
    val dayDiff = (trigger.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 366 +
        trigger.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR)
    return when (dayDiff) {
        0 -> time
        1 -> "Tomorrow $time"
        else -> "${SimpleDateFormat("dd MMM", Locale.US).format(Date(triggerAtMillis))}, $time"
    }
}

private fun reminderLabel(reminder: BookingReminderPlan, nowMillis: Long): String = when (reminder) {
    is BookingReminderPlan.Scheduled -> "Reminder ${formatReminderTime(reminder.triggerAtMillis, nowMillis)}"
    is BookingReminderPlan.NotScheduled -> when (reminder.reason) {
        NoReminderReason.CANCELLED, NoReminderReason.COMPLETED, NoReminderReason.UNKNOWN_TIME -> "No reminder"
        NoReminderReason.PAST -> "Reminder time passed"
    }
}

@Composable
private fun BookingRowCard(
    booking: BookingItem,
    reminder: BookingReminderPlan,
    nowMillis: Long,
    onViewClick: () -> Unit,
    onCancelClick: () -> Unit,
    onMarkArrived: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.customerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "${booking.membershipPlan} · ${booking.customerEmail}",
                        fontSize = 11.sp,
                        color = ByceCoolGray
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShiftBadge(shift = booking.shift)
                    ArrivalStatusBadge(arrivalStatus = booking.arrivalStatus)
                    OwnerStatusBadge(status = booking.status)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time & staff reminder tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${booking.date} · ${booking.time}",
                        fontSize = 12.sp,
                        color = TextWhite
                    )
                }

                // Staff reminder tag
                val isScheduled = reminder is BookingReminderPlan.Scheduled
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isScheduled) StatusInfoBlueBg else StatusCancelledGrayBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isScheduled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (isScheduled) StatusInfoBlue else StatusCancelledGray,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reminderLabel(reminder, nowMillis),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isScheduled) StatusInfoBlue else StatusCancelledGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (booking.arrivalStatus != "Checked In" && booking.status != "Cancelled") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(StatusActiveGreen.copy(alpha = 0.35f), StatusActiveGreen.copy(alpha = 0.15f))
                                    )
                                )
                                .clickable { onMarkArrived() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = "Arrived", tint = StatusActiveGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Mark Arrived",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusActiveGreen
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x20FFFFFF))
                            .clickable { onViewClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "View",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    if (booking.status == "Upcoming") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusExpiredRedBg)
                                .clickable { onCancelClick() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusExpiredRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShiftBadge(shift: String) {
    val (bgColor, textColor, icon) = when {
        shift.contains("Morning", ignoreCase = true) -> Triple(Color(0x2AF59E0B), Color(0xFFF59E0B), "🌅")
        shift.contains("Evening", ignoreCase = true) -> Triple(Color(0x2AF97316), Color(0xFFF97316), "🌇")
        shift.contains("Night", ignoreCase = true) -> Triple(Color(0x2A818CF8), Color(0xFF818CF8), "🌙")
        else -> Triple(Color(0x20FFFFFF), TextWhite, "⚡")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$icon ${shift.replace(" Shift", "")}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun ArrivalStatusBadge(arrivalStatus: String) {
    val (bgColor, textColor, label) = when (arrivalStatus) {
        "Checked In" -> Triple(StatusActiveGreenBg, StatusActiveGreen, "✓ Checked In")
        "Not Arrived" -> Triple(StatusPendingAmberBg, StatusPendingAmber, "Not Arrived")
        else -> Triple(Color(0x25FFFFFF), TextWhite, arrivalStatus)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun BookingDetailModal(
    booking: BookingItem,
    reminder: BookingReminderPlan,
    nowMillis: Long,
    notificationsAllowed: Boolean,
    onDismiss: () -> Unit,
    onUpdateShift: (String) -> Unit,
    onCancelBooking: () -> Unit,
    onMarkArrived: () -> Unit = {}
) {
    var selectedShift by remember { mutableStateOf(booking.shift) }

    OwnerModalDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Booking & Shift Details", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = booking.customerName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Text(text = "Email: ${booking.customerEmail} · Phone: ${booking.customerPhone}", fontSize = 12.sp, color = ByceCoolGray)

            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x20FFFFFF)))
            Spacer(modifier = Modifier.height(12.dp))

            BookingDetailRow("Booking ID", booking.id)
            BookingDetailRow("Plan", booking.membershipPlan)
            BookingDetailRow("Scheduled Date", booking.date)
            BookingDetailRow("Slot Window", booking.time)
            BookingDetailRow("Gym Location", "Iron House Fitness, Kozhikode")
            BookingDetailRow("Arrival Status", booking.arrivalStatus)

            Spacer(modifier = Modifier.height(8.dp))

            // Shift Selector Row
            Text(text = "Booked Shift Window:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Morning Shift", "Evening Shift", "Night Shift").forEach { shiftOption ->
                    val isCurrent = selectedShift == shiftOption
                    val (icon, color) = when (shiftOption) {
                        "Morning Shift" -> Pair("🌅", Color(0xFFF59E0B))
                        "Evening Shift" -> Pair("🌇", Color(0xFFF97316))
                        else -> Pair("🌙", Color(0xFF818CF8))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurrent) color.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                            .clickable {
                                selectedShift = shiftOption
                                onUpdateShift(shiftOption)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$icon ${shiftOption.replace(" Shift", "")}",
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) color else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ----------------------------------------------------------------
            // STAFF REMINDER ON THIS DEVICE
            // ----------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(OwnerModalSurfaceElevated)
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = StatusInfoBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Staff Reminder", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusInfoBlueBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "This device", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusInfoBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A notification appears on this phone ${BookingReminderPlanner.MINUTES_BEFORE} minutes before the session starts. The member isn't notified.",
                        fontSize = 11.sp,
                        color = ByceCoolGray,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x18FFFFFF)))
                    Spacer(modifier = Modifier.height(8.dp))

                    BookingDetailRow(
                        "Reminder",
                        when (reminder) {
                            is BookingReminderPlan.Scheduled -> formatReminderTime(reminder.triggerAtMillis, nowMillis)
                            is BookingReminderPlan.NotScheduled -> when (reminder.reason) {
                                NoReminderReason.CANCELLED -> "None (cancelled)"
                                NoReminderReason.COMPLETED -> "None (completed)"
                                NoReminderReason.PAST -> "Time has passed"
                                NoReminderReason.UNKNOWN_TIME -> "None (no start time)"
                            }
                        }
                    )
                    BookingDetailRow("Shown to", "Staff on this device")
                    if (!notificationsAllowed && reminder is BookingReminderPlan.Scheduled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notifications are off for BYCE Partner, so this reminder won't appear.",
                            fontSize = 11.sp,
                            color = StatusPendingAmber,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (booking.arrivalStatus != "Checked In" && booking.status != "Cancelled") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(StatusActiveGreen.copy(alpha = 0.35f), StatusActiveGreen.copy(alpha = 0.15f))
                            )
                        )
                        .clickable { onMarkArrived() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✓ Mark Arrived & Check In", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusActiveGreen)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (booking.status == "Upcoming") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(StatusExpiredRedBg)
                        .clickable { onCancelBooking() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Cancel Booking", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusExpiredRed)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x22FFFFFF))
                    .clickable { onDismiss() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Close", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

@Composable
private fun BookingDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
    }
}
