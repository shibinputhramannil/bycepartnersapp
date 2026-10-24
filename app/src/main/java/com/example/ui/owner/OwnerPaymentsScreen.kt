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
import com.example.data.models.PaymentItem
import com.example.data.services.GymOwnerRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun OwnerPaymentsScreen(
    modifier: Modifier = Modifier
) {
    // Read-only: members pay through BYCE, so this screen has no collection or POS tools
    var selectedSection by remember { mutableStateOf("Online") } // "Online", "Pending"
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("CSV") }

    val allPayments = remember { GymOwnerRepository.getPayments() }
    val allPendingPayments = remember { GymOwnerRepository.getPendingPayments() }
    var selectedPaymentForDetail by remember { mutableStateOf<PaymentItem?>(null) }

    val filteredPayments = remember(searchQuery, selectedStatusFilter, allPayments) {
        allPayments.filter { payment ->
            val matchesQuery = payment.customerName.contains(searchQuery, ignoreCase = true) ||
                    payment.transactionId.contains(searchQuery, ignoreCase = true) ||
                    payment.provider.contains(searchQuery, ignoreCase = true)
            val matchesStatus = if (selectedStatusFilter == "All") true else payment.status.equals(selectedStatusFilter, ignoreCase = true)
            matchesQuery && matchesStatus
        }
    }

    val filteredPendingPayments = remember(searchQuery, allPendingPayments) {
        allPendingPayments.filter { pending ->
            pending.customerName.contains(searchQuery, ignoreCase = true) ||
                    pending.membershipPlan.contains(searchQuery, ignoreCase = true) ||
                    pending.phoneNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    // Summary figures come from the ledger and dues lists shown below
    val successfulPayments = allPayments.filter { it.status.equals("Successful", ignoreCase = true) }
    val failedCount = allPayments.count { it.status.equals("Failed", ignoreCase = true) }
    val successfulTotal = successfulPayments.sumOf { it.amount }
    val totalPendingAmount = allPendingPayments.sumOf { it.amount }

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
                            text = "MEMBER PAYMENTS VIA BYCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextSubtle,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Payments",
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

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Members pay through BYCE. This view is read-only.",
                    fontSize = 12.sp,
                    color = ByceCoolGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Everything on this screen is sample data until it is connected to BYCE
                DemoDataNotice(text = "Demo data: these figures and entries are samples, not live BYCE payments.")

                Spacer(modifier = Modifier.height(14.dp))

                // Section Tabs: Online | Pending Dues
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x18FFFFFF))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        Triple("Online", "Online Ledger", Icons.Default.CreditCard),
                        Triple("Pending", "Pending Dues (${allPendingPayments.size})", Icons.Default.PendingActions)
                    ).forEach { (secKey, title, icon) ->
                        val isSelected = selectedSection == secKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0x35FFFFFF) else Color.Transparent)
                                .clickable { selectedSection = secKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) TextWhite else TextMuted,
                                    modifier = Modifier.size(13.dp)
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

                Spacer(modifier = Modifier.height(12.dp))

                // Top summary cards (2x2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniPaymentKpi("Successful Total", "₹${successfulTotal.toInt()}", TextWhite, Modifier.weight(1f))
                    MiniPaymentKpi("Pending Dues", "₹${totalPendingAmount.toInt()}", StatusPendingAmber, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniPaymentKpi("Successful", "${successfulPayments.size}", TextWhite, Modifier.weight(1f))
                    MiniPaymentKpi("Failed", "$failedCount", StatusExpiredRed, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                OwnerSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search transaction, member, phone...",
                    filterOptions = listOf("All", "Successful", "Failed"),
                    selectedFilter = selectedStatusFilter,
                    onFilterSelected = { selectedStatusFilter = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                val countText = when (selectedSection) {
                    "Pending" -> "Showing ${filteredPendingPayments.size} demo pending dues"
                    else -> "Showing ${filteredPayments.size} demo transactions"
                }
                Text(
                    text = countText,
                    fontSize = 12.sp,
                    color = ByceCoolGray
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Section 1: Pending Dues (read-only)
            if (selectedSection == "Pending") {
                if (filteredPendingPayments.isEmpty()) {
                    item {
                        OwnerEmptyState(
                            title = "No Pending Dues",
                            description = "All membership and subscription payments are up to date."
                        )
                    }
                } else {
                    items(filteredPendingPayments, key = { it.id }) { pending ->
                        PendingPaymentCardRow(item = pending)
                    }
                }
            }
            // Section 2: Online Ledger
            else {
                if (filteredPayments.isEmpty()) {
                    item {
                        OwnerEmptyState(
                            title = "No Payments Found",
                            description = "No transaction matches filter '$selectedStatusFilter'."
                        )
                    }
                } else {
                    items(filteredPayments, key = { it.transactionId }) { payment ->
                        PaymentRowCard(
                            payment = payment,
                            onViewClick = { selectedPaymentForDetail = payment }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        selectedPaymentForDetail?.let { payment ->
            PaymentDetailModal(
                payment = payment,
                onDismiss = { selectedPaymentForDetail = null }
            )
        }

        if (showExportDialog) {
            ExportModalDialog(
                moduleName = "Payments & Transactions",
                selectedFormat = exportFormat,
                onFormatChange = { exportFormat = it },
                onDismiss = { showExportDialog = false }
            )
        }
    }
}

// Read-only: members pay BYCE, so partners don't collect or record these dues
@Composable
private fun PendingPaymentCardRow(
    item: com.example.data.models.PendingPaymentItem
) {
    val isOverdue = item.status.contains("Overdue", ignoreCase = true) || item.status.contains("Failed", ignoreCase = true)
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.customerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.phoneNumber} · ${item.membershipPlan}",
                        fontSize = 12.sp,
                        color = ByceCoolGray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${item.amount.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isOverdue) StatusExpiredRed else StatusPendingAmber
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isOverdue) StatusExpiredRedBg else StatusPendingAmberBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverdue) StatusExpiredRed else StatusPendingAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (isOverdue) StatusExpiredRed else ByceCoolGray,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.dueDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOverdue) StatusExpiredRed else ByceCoolGray
                )
            }
        }
    }
}

@Composable
private fun MiniPaymentKpi(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(text = label, fontSize = 11.sp, color = ByceCoolGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@Composable
private fun PaymentRowCard(
    payment: PaymentItem,
    onViewClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x20FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(18.dp)
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
                        text = payment.customerName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "₹${payment.amount.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${payment.transactionId} · ${payment.provider}",
                        fontSize = 11.sp,
                        color = ByceCoolGray
                    )
                    OwnerStatusBadge(status = payment.status)
                }

                Text(
                    text = "${payment.planName} · ${payment.date}",
                    fontSize = 11.sp,
                    color = TextSubtle
                )
            }
        }
    }
}

@Composable
private fun PaymentDetailModal(
    payment: PaymentItem,
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
                Text(text = "Payment Receipt", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "₹${payment.amount.toInt()}",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
            Text(text = "Transaction ${payment.transactionId}", fontSize = 12.sp, color = ByceCoolGray)

            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x20FFFFFF)))
            Spacer(modifier = Modifier.height(12.dp))

            ReceiptRow("Customer", payment.customerName)
            ReceiptRow("Plan", payment.planName)
            ReceiptRow("Provider", payment.provider)
            ReceiptRow("Order ID", payment.orderId)
            ReceiptRow("Payment ID", payment.paymentId)
            ReceiptRow("Date", payment.date)
            ReceiptRow("Paid At", payment.paidAt)

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Status", fontSize = 13.sp, color = TextMuted)
                OwnerStatusBadge(status = payment.status)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0x35FFFFFF), Color(0x20FFFFFF))
                        )
                    )
                    .clickable { onDismiss() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Close Receipt", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
    }
}
