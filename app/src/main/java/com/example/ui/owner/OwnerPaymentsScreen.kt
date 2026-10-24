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
import com.example.data.models.PosInvoiceItem
import com.example.data.services.GymOwnerRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun OwnerPaymentsScreen(
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf("Online") } // "Online", "Pending", "POS"
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("CSV") }
    var paymentToRecordPaid by remember { mutableStateOf<com.example.data.models.PendingPaymentItem?>(null) }
    var reminderToastMessage by remember { mutableStateOf<String?>(null) }
    var showNewPosModal by remember { mutableStateOf(false) }
    var selectedPosInvoiceForDetail by remember { mutableStateOf<PosInvoiceItem?>(null) }

    val allPayments = remember { GymOwnerRepository.getPayments() }
    val allPendingPayments = remember(refreshTrigger) { GymOwnerRepository.getPendingPayments() }
    val allPosInvoices = remember(refreshTrigger) { GymOwnerRepository.getPosInvoices() }
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

    val filteredPendingPayments = remember(searchQuery, selectedStatusFilter, allPendingPayments) {
        allPendingPayments.filter { pending ->
            pending.customerName.contains(searchQuery, ignoreCase = true) ||
                    pending.membershipPlan.contains(searchQuery, ignoreCase = true) ||
                    pending.phoneNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredPosInvoices = remember(searchQuery, allPosInvoices) {
        allPosInvoices.filter { inv ->
            inv.customerName.contains(searchQuery, ignoreCase = true) ||
                    inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                    inv.itemName.contains(searchQuery, ignoreCase = true) ||
                    inv.transactionType.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalPendingAmount = remember(allPendingPayments) {
        allPendingPayments.sumOf { it.amount }
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
                            text = "FINANCIAL & BILLING LEDGER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextSubtle,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Payments & POS Desk",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedSection == "POS") {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF6B9330), Color(0xFF4E7320))
                                        )
                                    )
                                    .clickable { showNewPosModal = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = TextWhite, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "New POS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }
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
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section Tabs: Online | Pending Dues | Point of Sale (POS)
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
                        Triple("Pending", "Pending Dues (${allPendingPayments.size})", Icons.Default.PendingActions),
                        Triple("POS", "On-Site POS Desk", Icons.Default.PointOfSale)
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
                    MiniPaymentKpi("Total Revenue", "₹4,82,500", TextWhite, Modifier.weight(1f))
                    MiniPaymentKpi("Pending Dues", "₹${totalPendingAmount.toInt()}", StatusPendingAmber, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniPaymentKpi("Successful", "942", TextWhite, Modifier.weight(1f))
                    MiniPaymentKpi("On-Site POS Invoices", "${allPosInvoices.size} Generated", StatusInfoBlue, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                reminderToastMessage?.let { msg ->
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
                            IconButton(onClick = { reminderToastMessage = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = StatusActiveGreen, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (selectedSection != "POS") {
                    OwnerSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search transaction, member, phone...",
                        filterOptions = listOf("All", "Successful", "Failed"),
                        selectedFilter = selectedStatusFilter,
                        onFilterSelected = { selectedStatusFilter = it }
                    )
                } else {
                    OwnerSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search POS invoice #, member, or upgrade...",
                        filterOptions = listOf("All"),
                        selectedFilter = "All",
                        onFilterSelected = {}
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val countText = when (selectedSection) {
                    "Pending" -> "Showing ${filteredPendingPayments.size} pending dues"
                    "POS" -> "Showing ${filteredPosInvoices.size} on-site POS invoices"
                    else -> "Showing ${filteredPayments.size} online transactions"
                }
                Text(
                    text = countText,
                    fontSize = 12.sp,
                    color = ByceCoolGray
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Section 1: Pending Dues
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
                        PendingPaymentCardRow(
                            item = pending,
                            onRecordPayment = {
                                paymentToRecordPaid = pending
                            }
                        )
                    }
                }
            } 
            // Section 2: POS Invoices & Billing
            else if (selectedSection == "POS") {
                if (filteredPosInvoices.isEmpty()) {
                    item {
                        OwnerEmptyState(
                            title = "No POS Invoices",
                            description = "Click '+ New POS' to issue on-site membership upgrades, renewals, merchandise sales, or refunds."
                        )
                    }
                } else {
                    items(filteredPosInvoices, key = { it.id }) { inv ->
                        PosInvoiceCardRow(
                            invoice = inv,
                            onViewClick = { selectedPosInvoiceForDetail = inv }
                        )
                    }
                }
            }
            // Section 3: Online Ledger
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

        selectedPosInvoiceForDetail?.let { inv ->
            PosInvoiceReceiptModal(
                invoice = inv,
                onDismiss = { selectedPosInvoiceForDetail = null }
            )
        }

        paymentToRecordPaid?.let { item ->
            RecordPaymentConfirmDialog(
                item = item,
                onConfirm = {
                    GymOwnerRepository.recordPendingPaymentPaid(item.id)
                    paymentToRecordPaid = null
                    refreshTrigger++
                    reminderToastMessage = "✓ Recorded manual payment of ₹${item.amount.toInt()} for ${item.customerName}."
                },
                onDismiss = { paymentToRecordPaid = null }
            )
        }

        if (showNewPosModal) {
            NewPosInvoiceModal(
                onDismiss = { showNewPosModal = false },
                onCreateInvoice = { name, phone, type, itemDesc, baseAmt, mode ->
                    GymOwnerRepository.createPosInvoice(name, phone, type, itemDesc, baseAmt, mode)
                    refreshTrigger++
                    showNewPosModal = false
                    reminderToastMessage = "✓ Created POS Tax Invoice for $name (₹${(baseAmt * 1.18).toInt()})."
                }
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

@Composable
private fun PosInvoiceCardRow(
    invoice: PosInvoiceItem,
    onViewClick: () -> Unit
) {
    val isRefund = invoice.baseAmount < 0 || invoice.transactionType.contains("Refund", ignoreCase = true)

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewClick() },
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
                            .background(if (isRefund) Color(0x35EF4444) else Color(0x3538BDF8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRefund) Icons.Default.Replay else Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = if (isRefund) StatusExpiredRed else StatusInfoBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = invoice.customerName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text(text = "${invoice.invoiceNumber} · ${invoice.transactionType}", fontSize = 11.sp, color = ByceCoolGray)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${invoice.totalAmount.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isRefund) StatusExpiredRed else TextWhite
                    )
                    Text(text = invoice.paymentMode, fontSize = 10.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x15FFFFFF)))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = invoice.itemName, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Medium)
                OwnerStatusBadge(status = invoice.status)
            }
        }
    }
}

@Composable
private fun NewPosInvoiceModal(
    onDismiss: () -> Unit,
    onCreateInvoice: (name: String, phone: String, type: String, itemName: String, baseAmount: Double, paymentMode: String) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("On-Site Upgrade") }
    var itemName by remember { mutableStateOf("Upgrade to All-Access Elite (Annual)") }
    var baseAmountStr by remember { mutableStateOf("14999") }
    var paymentMode by remember { mutableStateOf("Razorpay POS Terminal") }

    val baseAmount = baseAmountStr.toDoubleOrNull() ?: 0.0
    val gstAmount = baseAmount * 0.18
    val totalAmount = baseAmount + gstAmount

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
                Text(text = "Point of Sale (POS) Billing", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LiquidGlassTextField(value = customerName, onValueChange = { customerName = it }, label = "Customer Name", placeholder = "e.g. Rahul Menon")
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassTextField(value = customerPhone, onValueChange = { customerPhone = it }, label = "Phone Number", placeholder = "+91 98471 22334")
            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Transaction Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))

            listOf("Membership Renewal", "On-Site Upgrade", "Merch Purchase", "Billing Refund").forEach { type ->
                val isSelected = transactionType == type
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0x25FFFFFF) else Color(0x0AFFFFFF))
                        .clickable { transactionType = type }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { transactionType = type },
                        colors = RadioButtonDefaults.colors(selectedColor = StatusInfoBlue, unselectedColor = TextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = type, fontSize = 12.sp, color = TextWhite)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassTextField(value = itemName, onValueChange = { itemName = it }, label = "Item / Plan Description", placeholder = "e.g. All-Access Elite Annual")
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassTextField(value = baseAmountStr, onValueChange = { baseAmountStr = it }, label = "Base Amount (₹)", placeholder = "14999")
            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Razorpay POS", "Dynamic UPI", "Card", "Cash").forEach { mode ->
                    val isSelected = paymentMode.contains(mode)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0x3538BDF8) else Color(0x15FFFFFF))
                            .clickable { paymentMode = mode }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = mode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) StatusInfoBlue else TextWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // GST Breakdown Card
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    ReceiptRow("Base Amount", "₹${baseAmount.toInt()}")
                    ReceiptRow("GST (18% SGST+CGST)", "₹${gstAmount.toInt()}")
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x15FFFFFF)).padding(vertical = 2.dp))
                    ReceiptRow("Total Billable", "₹${totalAmount.toInt()}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Disabled until a payment provider is connected
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x12FFFFFF))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Generate Tax Invoice & Charge", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Payment processing: Not available yet",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun PosInvoiceReceiptModal(
    invoice: PosInvoiceItem,
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
                Text(text = "Tax Invoice Receipt", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "₹${invoice.totalAmount.toInt()}", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
            Text(text = "Invoice Number: ${invoice.invoiceNumber}", fontSize = 12.sp, color = ByceCoolGray)

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x20FFFFFF)))
            Spacer(modifier = Modifier.height(10.dp))

            ReceiptRow("Customer", invoice.customerName)
            ReceiptRow("Phone", invoice.customerPhone)
            ReceiptRow("Transaction", invoice.transactionType)
            ReceiptRow("Item", invoice.itemName)
            ReceiptRow("Base Amount", "₹${invoice.baseAmount.toInt()}")
            ReceiptRow("GST Tax (18%)", "₹${invoice.gstTax.toInt()}")
            ReceiptRow("Payment Mode", invoice.paymentMode)
            ReceiptRow("Staff", invoice.billingStaff)
            ReceiptRow("Timestamp", invoice.timestamp)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Status", fontSize = 13.sp, color = TextMuted)
                OwnerStatusBadge(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x25FFFFFF))
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
private fun PendingPaymentCardRow(
    item: com.example.data.models.PendingPaymentItem,
    onRecordPayment: () -> Unit
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Send Reminder Button (disabled until SMS/WhatsApp messaging is connected)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x10FFFFFF))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Remind (not available yet)",
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Remind · Not available yet", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }
                    }

                    // Record Payment Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(StatusActiveGreen.copy(alpha = 0.3f), StatusActiveGreen.copy(alpha = 0.15f))
                                )
                            )
                            .clickable { onRecordPayment() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Record Paid",
                                tint = StatusActiveGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Record Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusActiveGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordPaymentConfirmDialog(
    item: com.example.data.models.PendingPaymentItem,
    onConfirm: () -> Unit,
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
                Text(text = "Record Payment", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Confirm manual payment collection for this member:",
                fontSize = 12.sp,
                color = ByceCoolGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(OwnerModalSurfaceElevated)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    ReceiptRow("Member", item.customerName)
                    ReceiptRow("Phone", item.phoneNumber)
                    ReceiptRow("Plan", item.membershipPlan)
                    ReceiptRow("Amount", "₹${item.amount.toInt()}")
                    ReceiptRow("Status", item.dueDate)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x20FFFFFF))
                        .clickable { onDismiss() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Cancel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6B9330), Color(0xFF4E7320))
                            )
                        )
                        .clickable { onConfirm() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Mark Paid", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
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
