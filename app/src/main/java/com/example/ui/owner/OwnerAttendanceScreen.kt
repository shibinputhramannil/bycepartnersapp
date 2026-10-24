package com.example.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.*
import com.example.data.services.GymOwnerRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun OwnerAttendanceScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedFilter by remember { mutableStateOf("Today") }
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showManualCheckInDialog by remember { mutableStateOf(false) }
    var showManualOverrideDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("CSV") }
    var showCameraScannerModal by remember { mutableStateOf(false) }
    val activeTrialAlert = GymOwnerRepository.getActiveTrialBuzzer()

    // Modal dialog states for the KPI cards
    var showTodayCheckinsModal by remember { mutableStateOf(false) }
    var showCurrentlyInsideModal by remember { mutableStateOf(false) }
    var showCompletedVisitsModal by remember { mutableStateOf(false) }

    val summary = remember(refreshTrigger) { GymOwnerRepository.getAttendanceSummary() }
    val attendanceList = remember(refreshTrigger) { GymOwnerRepository.getAttendanceRecords() }

    val filteredList = attendanceList.filter { item ->
        val matchesTab = when (selectedFilter) {
            "Today" -> item.date == "Today"
            "Yesterday" -> item.date == "Yesterday"
            else -> true
        }
        val matchesSearch = item.customerName.contains(searchQuery, ignoreCase = true) ||
                item.membershipPlan.contains(searchQuery, ignoreCase = true) ||
                item.customerId.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesSearch
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Page Title & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CHECK-IN & VISITS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSubtle,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Attendance / Check-ins",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Manual Override Button (Hardware Bypass)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(StatusExpiredRedBg)
                            .clickable { showManualOverrideDialog = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = "Override", tint = StatusExpiredRed, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Override", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusExpiredRed)
                        }
                    }

                    // Camera Scan QR Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(StatusActiveGreen)
                            .clickable { showCameraScannerModal = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR", tint = Color.Black, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // Export Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x20FFFFFF))
                            .clickable { showExportDialog = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export", tint = TextWhite, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Export", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        }
                    }

                    // Manual Check-in Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x35FFFFFF))
                            .clickable { showManualCheckInDialog = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Check-in", tint = TextWhite, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Check-in", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Summary Cards (4 Interactive Metrics)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AttendanceKpiBox(
                    title = "Today's Check-ins",
                    value = summary.todayCheckins.toString(),
                    icon = Icons.Default.QrCodeScanner,
                    subLabel = "Tap for List",
                    modifier = Modifier.weight(1f),
                    onClick = { showTodayCheckinsModal = true }
                )
                AttendanceKpiBox(
                    title = "Currently Inside",
                    value = summary.currentlyInside.toString(),
                    icon = Icons.Default.DirectionsRun,
                    subLabel = "Tap for List",
                    modifier = Modifier.weight(1f),
                    onClick = { showCurrentlyInsideModal = true }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AttendanceKpiBox(
                    title = "Completed Visits",
                    value = summary.completedVisits.toString(),
                    icon = Icons.Default.CheckCircleOutline,
                    subLabel = "Tap for History",
                    modifier = Modifier.weight(1f),
                    onClick = { showCompletedVisitsModal = true }
                )
                AttendanceKpiBox(
                    title = "Avg Daily Visits",
                    value = summary.avgDailyVisits.toString(),
                    icon = Icons.Default.BarChart,
                    subLabel = null,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar & Filter Tabs
            OwnerSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search by customer name, plan or ID...",
                selectedFilter = selectedFilter,
                filterOptions = listOf("Today", "Yesterday", "Date Range"),
                onFilterSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Records List
            if (filteredList.isEmpty()) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "No check-ins found for selected filter", fontSize = 13.sp, color = TextMuted)
                        }
                    }
                }
            } else {
                filteredList.forEach { record ->
                    AttendanceRecordCard(
                        record = record,
                        onCheckOut = {
                            GymOwnerRepository.recordCheckOut(record.id)
                            refreshTrigger++
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // --------------------------------------------------------------------
        // 1. TODAY'S CHECK-INS MODAL
        // --------------------------------------------------------------------
        if (showTodayCheckinsModal) {
            TodayCheckinsModal(
                checkins = attendanceList.filter { it.date == "Today" },
                onDismiss = { showTodayCheckinsModal = false }
            )
        }

        // --------------------------------------------------------------------
        // 2. CURRENTLY INSIDE MODAL
        // --------------------------------------------------------------------
        if (showCurrentlyInsideModal) {
            CurrentlyInsideModal(
                insideMembers = attendanceList.filter { it.status == "Checked In" && it.date == "Today" },
                onCheckOut = { recordId ->
                    GymOwnerRepository.recordCheckOut(recordId)
                    refreshTrigger++
                },
                onDismiss = { showCurrentlyInsideModal = false }
            )
        }

        // --------------------------------------------------------------------
        // 3. COMPLETED VISITS MODAL
        // --------------------------------------------------------------------
        if (showCompletedVisitsModal) {
            CompletedVisitsModal(
                completedMembers = attendanceList.filter { it.status == "Completed" },
                onDismiss = { showCompletedVisitsModal = false }
            )
        }

        // Manual Check-in Dialog
        // Manual Check-in Dialog
        if (showManualCheckInDialog) {
            ManualCheckInDialog(
                onDismiss = { showManualCheckInDialog = false },
                onCheckIn = { customerId, reason ->
                    GymOwnerRepository.checkIn(customerId = customerId, reason = reason, source = "manual check-in").also {
                        if (it is CheckInOutcome.Success) refreshTrigger++
                    }
                }
            )
        }

        // 1. Manual Attendance Override Dialog (QR pass can't be scanned)
        if (showManualOverrideDialog) {
            ManualAttendanceOverrideDialog(
                onDismiss = { showManualOverrideDialog = false },
                onConfirmOverride = { customerId, reason ->
                    GymOwnerRepository.performAttendanceOverride(customerId = customerId, reason = reason).also {
                        if (it is CheckInOutcome.Success) {
                            refreshTrigger++
                            showManualOverrideDialog = false
                        }
                    }
                }
            )
        }

        // 5. Full-Screen Incoming-Call Style Trial Buzzer Alert
        activeTrialAlert?.let { alert ->
            if (!alert.isAcknowledged) {
                TrialCheckInBuzzerDialog(
                    alert = alert,
                    onAcknowledge = {
                        GymOwnerRepository.acknowledgeTrialAlert(alert.id, "Staff Kishore")
                        refreshTrigger++
                    },
                    onDismiss = {
                        GymOwnerRepository.dismissTrialBuzzer()
                        refreshTrigger++
                    }
                )
            }
        }

        // Export Dialog
        if (showExportDialog) {
            ExportModalDialog(
                moduleName = "Attendance & Check-in Records",
                selectedFormat = exportFormat,
                onFormatChange = { exportFormat = it },
                onDismiss = { showExportDialog = false }
            )
        }

        // Camera QR Scanner Modal
        if (showCameraScannerModal) {
            OwnerQrScannerModal(
                onDismiss = { showCameraScannerModal = false },
                onScannedSuccessfully = {
                    refreshTrigger++
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// KPI CARD COMPOSABLE (WITH TAP HINT)
// ---------------------------------------------------------------------------
@Composable
private fun AttendanceKpiBox(
    title: String,
    value: String,
    icon: ImageVector,
    subLabel: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    LiquidGlassCard(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = TextSubtle, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            if (subLabel != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(StatusActiveGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusActiveGreen
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ATTENDANCE RECORD ROW CARD
// ---------------------------------------------------------------------------
@Composable
private fun AttendanceRecordCard(
    record: AttendanceItem,
    onCheckOut: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = record.customerName.take(2).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = record.customerName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Text(
                            text = "ID: ${record.customerId} · ${record.membershipPlan}",
                            fontSize = 11.sp,
                            color = ByceCoolGray
                        )
                    }
                }

                OwnerStatusBadge(status = record.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x15FFFFFF)))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "In: ${record.checkInTime}" + (if (record.checkOutTime != null) " · Out: ${record.checkOutTime}" else " · Currently Inside"),
                        fontSize = 11.sp,
                        color = if (record.checkOutTime == null) StatusActiveGreen else TextMuted
                    )
                }

                if (record.status == "Checked In") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x28FFFFFF))
                            .clickable { onCheckOut() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(text = "Check Out", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                } else if (record.bookedSlot != null) {
                    Text(text = "Slot: ${record.bookedSlot}", fontSize = 11.sp, color = ByceCoolGray)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 1. TODAY'S CHECK-INS MODAL
// ---------------------------------------------------------------------------
@Composable
private fun TodayCheckinsModal(
    checkins: List<AttendanceItem>,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = checkins.filter {
        it.customerName.contains(query, ignoreCase = true) || it.membershipPlan.contains(query, ignoreCase = true)
    }

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
                Column {
                    Text(text = "Today's Check-ins", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(text = "${checkins.size} Total Visitors Today", fontSize = 11.sp, color = ByceCoolGray)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LiquidGlassTextField(
                value = query,
                onValueChange = { query = it },
                label = "Filter Today's Check-ins",
                placeholder = "Search member name..."
            )

            Spacer(modifier = Modifier.height(14.dp))

            filtered.forEach { item ->
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = OwnerModalSurfaceElevated
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x25FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.customerName.take(2).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = item.customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(text = "${item.membershipPlan} · In: ${item.checkInTime}", fontSize = 11.sp, color = ByceCoolGray)
                            }
                        }

                        OwnerStatusBadge(status = item.status)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x25FFFFFF))
                    .clickable { onDismiss() }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Close", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2. CURRENTLY INSIDE MODAL
// ---------------------------------------------------------------------------
@Composable
private fun CurrentlyInsideModal(
    insideMembers: List<AttendanceItem>,
    onCheckOut: (String) -> Unit,
    onDismiss: () -> Unit
) {
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(StatusActiveGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Currently Inside Gym", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                    Text(text = "${insideMembers.size} Active Members Inside · Capacity: 100", fontSize = 11.sp, color = ByceCoolGray)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (insideMembers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No members currently inside.", fontSize = 13.sp, color = TextMuted)
                }
            } else {
                insideMembers.forEach { member ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = OwnerModalSurfaceElevated,
                        borderWidth = 1.dp,
                        borderColor = Color(0x25FFFFFF)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = member.customerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text(text = "ID: ${member.customerId} · ${member.membershipPlan}", fontSize = 11.sp, color = ByceCoolGray)
                                }

                                // Real check-in time from the attendance record
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x3034D399))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = StatusActiveGreen, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "In: ${member.checkInTime}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = StatusActiveGreen)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x28FFFFFF))
                                        .clickable { onCheckOut(member.id) }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Check Out Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x25FFFFFF))
                    .clickable { onDismiss() }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Close", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. COMPLETED VISITS MODAL
// ---------------------------------------------------------------------------
@Composable
private fun CompletedVisitsModal(
    completedMembers: List<AttendanceItem>,
    onDismiss: () -> Unit
) {
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
                Column {
                    Text(text = "Completed Visits", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(text = "${completedMembers.size} Recorded Checkout Sessions Today", fontSize = 11.sp, color = ByceCoolGray)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Full historical log of members who completed their gym workout and checked out today.",
                fontSize = 11.sp,
                color = TextSubtle
            )

            Spacer(modifier = Modifier.height(14.dp))

            completedMembers.forEach { member ->
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = OwnerModalSurfaceElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = member.customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusActiveGreenBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "✓ Completed", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StatusActiveGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(text = "${member.membershipPlan} · ID: ${member.customerId}", fontSize = 11.sp, color = ByceCoolGray)

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "In: ${member.checkInTime} ➔ Out: ${member.checkOutTime ?: "11:35 AM"}",
                                fontSize = 11.sp,
                                color = TextWhite
                            )
                            Text(
                                text = "Duration: 1h 31m",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ByceCoolGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x25FFFFFF))
                    .clickable { onDismiss() }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Close", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// MANUAL CHECK-IN DIALOG
// ---------------------------------------------------------------------------
@Composable
private fun ManualCheckInDialog(
    onDismiss: () -> Unit,
    onCheckIn: (customerId: String, reason: String?) -> CheckInOutcome
) {
    var selectedMember by remember { mutableStateOf<CustomerItem?>(null) }
    var rejection by remember { mutableStateOf<String?>(null) }

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
                Text(text = "Manual Gym Check-in", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            MemberPicker(selected = selectedMember, onSelect = { selectedMember = it; rejection = null })

            selectedMember?.let { member ->
                Spacer(modifier = Modifier.height(14.dp))
                val check = remember(member) { GymOwnerRepository.evaluateMemberCheckIn(member.id) }
                CheckInGateSection(
                    check = check,
                    confirmLabel = "Confirm Check-in",
                    onConfirm = { reason ->
                        when (val outcome = onCheckIn(member.id, reason)) {
                            is CheckInOutcome.Success -> onDismiss()
                            is CheckInOutcome.Rejected -> rejection = outcome.message
                        }
                    }
                )
            }

            rejection?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, fontSize = 11.sp, color = StatusExpiredRed)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// REUSABLE EXPORT DIALOG (FEATURE 19)
// ---------------------------------------------------------------------------
@Composable
fun ExportModalDialog(
    moduleName: String,
    selectedFormat: String,
    onFormatChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
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
                Text(text = "Export $moduleName", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Select your preferred file format to download the records for this module.",
                fontSize = 12.sp,
                color = ByceCoolGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("CSV", "PDF").forEach { fmt ->
                    val isSelected = selectedFormat == fmt
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x35FFFFFF) else Color(0x15FFFFFF))
                            .clickable { onFormatChange(fmt) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = fmt, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) TextWhite else TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Disabled until file export is implemented
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x12FFFFFF))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Download $selectedFormat", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Export: Not available yet",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 1. MANUAL ATTENDANCE OVERRIDE DIALOG (WHEN A QR PASS CAN'T BE SCANNED)
// ---------------------------------------------------------------------------
@Composable
private fun ManualAttendanceOverrideDialog(
    onDismiss: () -> Unit,
    onConfirmOverride: (customerId: String, reason: String) -> CheckInOutcome
) {
    var selectedMember by remember { mutableStateOf<CustomerItem?>(null) }
    var overrideReason by remember { mutableStateOf("QR Scanner Not Working") }
    var rejection by remember { mutableStateOf<String?>(null) }

    OwnerModalDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = StatusExpiredRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "MANUAL OVERRIDE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = StatusExpiredRed, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Force Attendance to 'Attended'", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Record attendance manually when a member's QR pass can't be scanned. The reason is logged.",
                fontSize = 11.sp,
                color = ByceCoolGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            MemberPicker(selected = selectedMember, onSelect = { selectedMember = it; rejection = null })

            // Hard blocks (duplicate check-in) still apply; the override reason covers expired/pending/frozen
            val check = selectedMember?.let { member ->
                remember(member) { GymOwnerRepository.evaluateMemberCheckIn(member.id) }
            }
            if (check != null && check.eligibility != CheckInEligibility.ALLOWED) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = check.message,
                    fontSize = 11.sp,
                    color = if (check.eligibility == CheckInEligibility.BLOCKED) StatusExpiredRed else StatusPendingAmber
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = "Override Reason / Constraint", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))

            listOf(
                "QR Scanner Not Working",
                "Member Can't Show QR Pass",
                "Manager Approved Entry"
            ).forEach { reason ->
                val isSelected = overrideReason == reason
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0x28FFFFFF) else Color.Transparent)
                        .clickable { overrideReason = reason }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { overrideReason = reason },
                        colors = RadioButtonDefaults.colors(selectedColor = StatusExpiredRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = reason, fontSize = 12.sp, color = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val canOverride = selectedMember != null && check?.eligibility != CheckInEligibility.BLOCKED
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (canOverride) {
                            Brush.horizontalGradient(colors = listOf(Color(0xFFD32F2F), Color(0xFFB71C1C)))
                        } else {
                            Brush.horizontalGradient(colors = listOf(Color(0x12FFFFFF), Color(0x12FFFFFF)))
                        }
                    )
                    .clickable(enabled = canOverride) {
                        val member = selectedMember ?: return@clickable
                        val outcome = onConfirmOverride(member.id, overrideReason)
                        if (outcome is CheckInOutcome.Rejected) rejection = outcome.message
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = if (canOverride) TextWhite else TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Force Override to 'Attended'", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (canOverride) TextWhite else TextMuted)
                }
            }

            rejection?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, fontSize = 11.sp, color = StatusExpiredRed)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 5. FULL-SCREEN INCOMING-CALL STYLE TRIAL BUZZER ALERT
// ---------------------------------------------------------------------------
@Composable
private fun TrialCheckInBuzzerDialog(
    alert: TrialCheckInBuzzerAlert,
    onAcknowledge: () -> Unit,
    onDismiss: () -> Unit
) {
    OwnerModalDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1012), Color(0xFF161415), Color(0xFF111714))
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Urgent Siren Visual Pulse Rings
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color(0x35FF5252)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(StatusExpiredRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Trial arrival alert",
                        tint = TextWhite,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Alert Headline (visual only; no sound or push notification yet)
            Text(
                text = "⚡ FIRST-TIME TRIALIST CHECK-IN",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StatusExpiredRed,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Trial Member Has Arrived",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Member Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x20FFFFFF))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = alert.memberName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusActiveGreenBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "NEW TRIALIST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusActiveGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Session: ${alert.sessionType} (${alert.trialPassType})", fontSize = 12.sp, color = ByceCoolGray)
                    Text(text = "Arrived: ${alert.arrivalTime} · Phone: ${alert.phone}", fontSize = 11.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mandatory Protocol: Greet member at reception and provide a 5-minute facility & locker tour.",
                    fontSize = 11.sp,
                    color = ByceCoolGray,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Acknowledge & Assign Tour Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6B9330), Color(0xFF4E7320))
                        )
                    )
                    .clickable { onAcknowledge() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TextWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Acknowledge & Start 5-Min Tour", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Dismiss", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}
