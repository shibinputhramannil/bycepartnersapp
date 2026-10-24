package com.example.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CheckInCheck
import com.example.data.models.CheckInEligibility
import com.example.data.models.CustomerItem
import com.example.data.services.GymOwnerRepository
import com.example.ui.components.*
import com.example.ui.theme.*

private val reasonsForInactiveMembership = listOf("Renewing at front desk", "Payment proof shown", "Manager approved")
private val reasonsForFrozenOverride = listOf("Member ending pause early", "Manager approved")

/**
 * Search box that only lets staff pick an existing member record
 */
@Composable
fun MemberPicker(
    selected: CustomerItem?,
    onSelect: (CustomerItem?) -> Unit
) {
    var query by remember { mutableStateOf("") }

    if (selected != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x20FFFFFF))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = selected.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text(text = "ID: ${selected.id.uppercase()} · ${selected.membershipPlan}", fontSize = 11.sp, color = ByceCoolGray)
            }
            OwnerStatusBadge(status = selected.status)
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = { onSelect(null) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Change member", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
        return
    }

    LiquidGlassTextField(
        value = query,
        onValueChange = { query = it },
        label = "Find Member",
        placeholder = "Name, member ID, email, or phone"
    )

    val matches = remember(query) { GymOwnerRepository.searchCustomers(query) }
    if (query.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        if (matches.isEmpty()) {
            Text(text = "No matching member. Only existing members can be checked in.", fontSize = 11.sp, color = StatusExpiredRed)
        }
        matches.forEach { customer ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(customer) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = customer.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                    Text(text = "${customer.id.uppercase()} · ${customer.phone}", fontSize = 11.sp, color = ByceCoolGray)
                }
                OwnerStatusBadge(status = customer.status)
            }
        }
    }
}

/**
 * Shows the check-in rule for a member/booking and collects a reason when one is required.
 * BLOCKED shows no button; BLOCKED_OVERRIDABLE needs an explicit "Staff override" first.
 */
@Composable
fun CheckInGateSection(
    check: CheckInCheck,
    confirmLabel: String,
    onConfirm: (reason: String?) -> Unit
) {
    var selectedReason by remember(check) { mutableStateOf<String?>(null) }
    var overrideOpen by remember(check) { mutableStateOf(false) }

    val (noticeColor, noticeBg) = when (check.eligibility) {
        CheckInEligibility.ALLOWED -> StatusActiveGreen to StatusActiveGreenBg
        CheckInEligibility.NEEDS_REASON -> StatusPendingAmber to StatusPendingAmberBg
        CheckInEligibility.BLOCKED_OVERRIDABLE, CheckInEligibility.BLOCKED -> StatusExpiredRed to StatusExpiredRedBg
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(noticeBg)
            .padding(12.dp)
    ) {
        Text(text = check.message, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = noticeColor, lineHeight = 16.sp)
    }

    val reasons = when (check.eligibility) {
        CheckInEligibility.NEEDS_REASON -> reasonsForInactiveMembership
        CheckInEligibility.BLOCKED_OVERRIDABLE -> if (overrideOpen) reasonsForFrozenOverride else emptyList()
        else -> emptyList()
    }

    if (check.eligibility == CheckInEligibility.BLOCKED_OVERRIDABLE && !overrideOpen) {
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = { overrideOpen = true }) {
            Text(text = "Staff override…", fontSize = 12.sp, color = TextWhite)
        }
        return
    }

    if (reasons.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Reason (required, logged)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        reasons.forEach { reason ->
            val isSelected = selectedReason == reason
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color(0x28FFFFFF) else Color.Transparent)
                    .clickable { selectedReason = reason }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { selectedReason = reason },
                    colors = RadioButtonDefaults.colors(selectedColor = noticeColor, unselectedColor = TextMuted)
                )
                Text(text = reason, fontSize = 12.sp, color = TextWhite)
            }
        }
    }

    if (check.eligibility == CheckInEligibility.BLOCKED) return

    val needsReason = check.eligibility != CheckInEligibility.ALLOWED
    val enabled = !needsReason || selectedReason != null
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = { onConfirm(if (needsReason) selectedReason else null) },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = if (needsReason) noticeColor else StatusActiveGreen),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (needsReason) "$confirmLabel with Reason" else confirmLabel,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/**
 * Dialog wrapper around CheckInGateSection for screens without their own check-in dialog
 */
@Composable
fun CheckInGateDialog(
    title: String,
    check: CheckInCheck,
    onDismiss: () -> Unit,
    onConfirm: (reason: String?) -> Unit
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
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            CheckInGateSection(check = check, confirmLabel = "Check In", onConfirm = onConfirm)
        }
    }
}
