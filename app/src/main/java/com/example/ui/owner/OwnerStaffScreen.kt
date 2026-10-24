package com.example.ui.owner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.GroupWorkoutSession
import com.example.data.models.StaffMember
import com.example.data.services.GymOwnerRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun OwnerStaffScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Roster") } // "Roster", "Sessions"
    val staffList = remember { mutableStateListOf<StaffMember>().apply { addAll(GymOwnerRepository.getStaff()) } }
    var refreshKey by remember { mutableIntStateOf(0) }

    val workoutSessions = remember(refreshKey) { GymOwnerRepository.getWorkoutSessions() }

    var showAddStaffDialog by remember { mutableStateOf(false) }
    var sessionForAssignProxy by remember { mutableStateOf<GroupWorkoutSession?>(null) }
    var assignSuccessMessage by remember { mutableStateOf<String?>(null) }

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
                            text = "PERSONNEL & SESSIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextSubtle,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Trainers & Coaches",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    if (selectedTab == "Roster") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF6B9330), Color(0xFF4E7320))
                                    )
                                )
                                .clickable { showAddStaffDialog = true }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Add Staff", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2 Subtabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x18FFFFFF))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        Triple("Roster", "Staff Roster", Icons.Default.Badge),
                        Triple("Sessions", "Sessions & Coaches", Icons.Default.FitnessCenter)
                    ).forEach { (tabKey, title, icon) ->
                        val isSelected = selectedTab == tabKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0x35FFFFFF) else Color.Transparent)
                                .clickable { selectedTab = tabKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) TextWhite else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextWhite else TextMuted
                                )
                            }
                        }
                    }
                }

                assignSuccessMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusActiveGreenBg)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusActiveGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = msg, fontSize = 12.sp, color = StatusActiveGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            // Tab 1: Staff Roster
            if (selectedTab == "Roster") {
                items(staffList, key = { it.id }) { staff ->
                    StaffRowCard(
                        staff = staff,
                        onToggleStatus = {
                            val index = staffList.indexOfFirst { it.id == staff.id }
                            if (index != -1) {
                                val newStatus = if (staff.status == "Active") "Inactive" else "Active"
                                staffList[index] = staff.copy(status = newStatus)
                                GymOwnerRepository.toggleStaffStatus(staff.id)
                            }
                        }
                    )
                }
            }

            // Tab 2: Sessions & Coaches
            if (selectedTab == "Sessions") {
                item {
                    Text(
                        text = "Allocated Group Workout Sessions & Capacity Tracker",
                        fontSize = 12.sp,
                        color = ByceCoolGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(workoutSessions, key = { it.id }) { session ->
                    GroupWorkoutSessionCard(
                        session = session,
                        onAssignSubstitute = {
                            sessionForAssignProxy = session
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Add Staff Dialog
        if (showAddStaffDialog) {
            AddStaffDialog(
                onDismiss = { showAddStaffDialog = false },
                onAdd = { newStaff ->
                    staffList.add(0, newStaff)
                    GymOwnerRepository.addStaff(newStaff)
                    showAddStaffDialog = false
                }
            )
        }

        // Assign Substitute Modal
        sessionForAssignProxy?.let { session ->
            AssignSubstituteModal(
                session = session,
                onDismiss = { sessionForAssignProxy = null },
                onAssign = { coachName ->
                    GymOwnerRepository.assignProxyTrainer(session.id, coachName)
                    assignSuccessMessage = "Successfully assigned '$coachName' to ${session.title}."
                    refreshKey++
                    sessionForAssignProxy = null
                }
            )
        }
    }
}

@Composable
private fun GroupWorkoutSessionCard(
    session: GroupWorkoutSession,
    onAssignSubstitute: () -> Unit
) {
    val occupancyPercent = if (session.capacity > 0) (session.bookedCount.toFloat() / session.capacity.toFloat()) else 0f

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                when (session.category) {
                                    "HRX" -> Color(0x35EF4444)
                                    "Boxing" -> Color(0x35F59E0B)
                                    "Yoga" -> Color(0x3510B981)
                                    "Dance" -> Color(0x35EC4899)
                                    else -> Color(0x3538BDF8)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = session.category.take(2).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = session.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text(text = "${session.roomName} · ${session.timeSlot}", fontSize = 11.sp, color = ByceCoolGray)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusActiveGreenBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = session.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusActiveGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coach & Capacity row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Trainer: ${session.coachName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                    Text(text = "Type: ${session.coachType}", fontSize = 10.sp, color = TextMuted)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${session.bookedCount}/${session.capacity} Booked (${(occupancyPercent * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(text = "Intensity: ${session.intensity}", fontSize = 10.sp, color = ByceCoolGray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Capacity Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x20FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(occupancyPercent.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (occupancyPercent >= 0.85f) Color(0xFFEF4444)
                            else if (occupancyPercent >= 0.6f) Color(0xFFF59E0B)
                            else Color(0xFF10B981)
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Assign Substitute Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x20FFFFFF))
                        .clickable { onAssignSubstitute() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, tint = TextWhite, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Assign Coach", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                    }
                }
            }
        }
    }
}

private val substituteCoachOptions = listOf(
    "Coach Rahul (Freelance Boxing & HIIT)",
    "Coach Divya (Certified Yoga & Mobility)",
    "Coach Sanjay (In-House S&C)",
    "Coach Anjali (Zumba Licensed)",
    "Coach Ramesh (Combat Trainer)"
)

// "Coach Divya (Certified Yoga & Mobility)" -> "Coach Divya"
internal fun substituteCoachName(option: String): String = option.substringBefore(" (").trim()

// Excludes the session's current coach so a coach can't substitute for themselves
internal fun substituteCandidates(options: List<String>, currentCoach: String): List<String> =
    options.filterNot { substituteCoachName(it).equals(currentCoach.trim(), ignoreCase = true) }

@Composable
private fun AssignSubstituteModal(
    session: GroupWorkoutSession,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit
) {
    var selectedCoach by remember { mutableStateOf<String?>(null) }
    val availableCoaches = remember(session.coachName) { substituteCandidates(substituteCoachOptions, session.coachName) }

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
                Text(text = "Assign Substitute Coach", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Session: ${session.title} (${session.timeSlot})", fontSize = 12.sp, color = ByceCoolGray)
            Text(text = "Primary Coach: ${session.coachName}", fontSize = 12.sp, color = TextMuted)

            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "Select Available Certified Coach", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(modifier = Modifier.height(8.dp))

            availableCoaches.forEach { coach ->
                val isSelected = selectedCoach == coach
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0x25FFFFFF) else Color(0x0AFFFFFF))
                        .clickable { selectedCoach = coach }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedCoach = coach },
                        colors = RadioButtonDefaults.colors(selectedColor = StatusInfoBlue, unselectedColor = TextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = coach, fontSize = 12.sp, color = TextWhite)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val chosen = selectedCoach
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (chosen != null) {
                            Brush.verticalGradient(colors = listOf(Color(0xFF6B9330), Color(0xFF4E7320)))
                        } else {
                            Brush.verticalGradient(colors = listOf(Color(0x12FFFFFF), Color(0x12FFFFFF)))
                        }
                    )
                    .clickable(enabled = chosen != null) { chosen?.let { onAssign(substituteCoachName(it)) } }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (chosen != null) "Confirm Coach Assignment" else "Select a coach to assign",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (chosen != null) TextWhite else TextMuted
                )
            }
        }
    }
}

@Composable
private fun StaffRowCard(
    staff: StaffMember,
    onToggleStatus: () -> Unit
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x20FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (staff.role == "Gym Admin") Icons.Default.AdminPanelSettings else Icons.Default.Badge,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = staff.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        OwnerStatusBadge(status = staff.status)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (staff.role == "Gym Admin") Color(0x3038BDF8) else Color(0x20FFFFFF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = staff.role,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (staff.role == "Gym Admin") StatusInfoBlue else TextWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Joined ${staff.createdDate}",
                            fontSize = 11.sp,
                            color = ByceCoolGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = staff.email, fontSize = 11.sp, color = TextMuted)
                    Text(text = staff.phone, fontSize = 11.sp, color = ByceCoolGray)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (staff.status == "Active") StatusExpiredRedBg else StatusActiveGreenBg)
                        .clickable { onToggleStatus() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (staff.status == "Active") "Deactivate" else "Activate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (staff.status == "Active") StatusExpiredRed else StatusActiveGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun AddStaffDialog(
    onDismiss: () -> Unit,
    onAdd: (StaffMember) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Staff") } // "Gym Admin" or "Staff"

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
                Text(text = "Add Staff Member", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LiquidGlassTextField(value = name, onValueChange = { name = it }, label = "Full Name", placeholder = "e.g. Ramesh Nair")
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "ramesh@ironhousefitness.com")
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassTextField(value = phone, onValueChange = { phone = it }, label = "Phone", placeholder = "+91 98470 55667")
            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Assign Role", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Staff", "Gym Admin").forEach { r ->
                    val isSelected = role == r
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x35FFFFFF) else Color(0x18FFFFFF))
                            .clickable { role = r }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = r,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF6B9330), Color(0xFF4E7320))
                        )
                    )
                    .clickable {
                        if (name.isNotBlank()) {
                            onAdd(
                                StaffMember(
                                    id = "st_${System.currentTimeMillis()}",
                                    name = name,
                                    email = email.ifBlank { "staff@ironhousefitness.com" },
                                    phone = phone.ifBlank { "+91 98470 00000" },
                                    role = role,
                                    status = "Active",
                                    createdDate = "24 Sep 2026"
                                )
                            )
                        }
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Add Member", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}
