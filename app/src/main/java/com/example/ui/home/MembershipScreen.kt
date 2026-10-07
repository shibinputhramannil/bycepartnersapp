package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.models.FreeTrialSession
import com.example.data.models.MembershipPlanItem
import com.example.data.services.GymOwnerRepository
import com.example.ui.components.FloatingBlurBalls
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Cult-Inspired Session & Multi-Tier Membership Experience
 */
@Composable
fun MembershipScreen(
    onBackClick: () -> Unit = {},
    onTabSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val plans = remember { GymOwnerRepository.getMembershipPlans() }
    val trialSessions = remember { mutableStateListOf<FreeTrialSession>().apply { addAll(GymOwnerRepository.getTrialSessions()) } }
    var freeCredits by remember { mutableStateOf(GymOwnerRepository.getFreeTrialCredits()) }

    var selectedDuration by remember { mutableStateOf("Annual (35% OFF)") } // "1 Month", "3 Months (15% OFF)", "Annual (35% OFF)"
    var selectedPlanId by remember { mutableStateOf("plan_elite_1") }
    var applyCorporateDiscount by remember { mutableStateOf(false) }
    var applyCardCashback by remember { mutableStateOf(false) }

    // Modals
    var showQrPassModal by remember { mutableStateOf(false) }
    var showPauseModal by remember { mutableStateOf(false) }
    var showGraceStartModal by remember { mutableStateOf(false) }
    var showPurchaseSuccessModal by remember { mutableStateOf(false) }
    var lastPurchasedPlan by remember { mutableStateOf<MembershipPlanItem?>(null) }
    var selectedGraceDays by remember { mutableStateOf(0) } // 0 = Start Today

    val scrollState = rememberScrollState()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val selectedPlan = plans.find { it.id == selectedPlanId } ?: plans.first()

    // Price calculation
    val basePrice = when (selectedDuration) {
        "1 Month" -> selectedPlan.monthlyPrice
        "3 Months (15% OFF)" -> selectedPlan.quarterlyPrice
        else -> selectedPlan.annualPrice
    }
    val corpDiscount = if (applyCorporateDiscount) basePrice * 0.20 else 0.0
    val cardCashback = if (applyCardCashback) (basePrice - corpDiscount) * 0.10 else 0.0
    val finalPrice = (basePrice - corpDiscount - cardCashback).coerceAtLeast(0.0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        FloatingBlurBalls()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = statusBarPadding + 12.dp, bottom = navBarPadding + 90.dp)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GlassSurfaceMedium)
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "FITNESS PASSPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextSubtle,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Membership & Sessions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }

                // Dynamic Entry QR Pass Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x35FFFFFF))
                        .clickable { showQrPassModal = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Pass", tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Scan Pass", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Active Membership Hero Card
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE MEMBERSHIP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextSubtle,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All-Access Elite Pass",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusActiveGreenBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusActiveGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Unlimited classes · City-wide premium access · 35 pause days left",
                        fontSize = 12.sp,
                        color = ByceCoolGray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x25FFFFFF))
                                .clickable { showPauseModal = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PauseCircleFilled, contentDescription = null, tint = TextWhite, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Pause Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF6B9330), Color(0xFF4E7320))
                                    )
                                )
                                .clickable { showQrPassModal = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = TextWhite, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Turnstile QR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ----------------------------------------------------
            // STEP 1: FREE TRIAL SESSIONS (2 Free Trial Credits)
            // ----------------------------------------------------
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "STEP 1: DISCOVERY & TRIAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonGreen,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Book 2 Free Trial Sessions",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x30FFFFFF))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$freeCredits Credits Left",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (freeCredits > 0) NeonGreen else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Experience premium group workouts and get a personalized facility tour at check-in with dynamic QR code.",
                        fontSize = 12.sp,
                        color = ByceCoolGray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    trialSessions.forEach { session ->
                        TrialSessionRow(
                            session = session,
                            canBook = freeCredits > 0 && session.status == "Available",
                            onBook = {
                                val booked = GymOwnerRepository.bookTrialSession(session.id)
                                if (booked) {
                                    val idx = trialSessions.indexOfFirst { it.id == session.id }
                                    if (idx != -1) {
                                        trialSessions[idx] = session.copy(status = "Booked")
                                    }
                                    freeCredits = GymOwnerRepository.getFreeTrialCredits()
                                    showQrPassModal = true
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ----------------------------------------------------
            // STEP 3: 5 MEMBERSHIP TIERS SELECTION & DURATION
            // ----------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MEMBERSHIP TIERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSubtle,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Choose Your Fitness Tier",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("1 Month", "3 Months (15% OFF)", "Annual (35% OFF)").forEach { dur ->
                    val isSelected = selectedDuration == dur
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x40FFFFFF) else Color(0x18FFFFFF))
                            .clickable { selectedDuration = dur }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dur,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextWhite else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5 Tier Cards List
            plans.forEach { plan ->
                CultTierCard(
                    plan = plan,
                    duration = selectedDuration,
                    isSelected = plan.id == selectedPlanId,
                    onSelect = { selectedPlanId = plan.id }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ----------------------------------------------------
            // CORPORATE INSURANCE & CARD CASHBACK INTEGRATIONS
            // ----------------------------------------------------
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "DISCOUNTS & BENEFITS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSubtle,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Corporate Insurance Flex Subsidy (20% OFF)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x18FFFFFF))
                            .clickable { applyCorporateDiscount = !applyCorporateDiscount }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = StatusActiveGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Corporate Health Insurance Subsidy", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                Text(text = "Instant 20% subsidy on annual/multi-month plans", fontSize = 11.sp, color = ByceCoolGray)
                            }
                        }
                        Checkbox(
                            checked = applyCorporateDiscount,
                            onCheckedChange = { applyCorporateDiscount = it },
                            colors = CheckboxDefaults.colors(checkedColor = StatusActiveGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bank Credit Card Cashback & No-Cost EMI (10% OFF)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x18FFFFFF))
                            .clickable { applyCardCashback = !applyCardCashback }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = StatusInfoBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Credit Card Cashback / No-Cost EMI", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                Text(text = "10% instant discount + 3-6 month 0% interest EMI", fontSize = 11.sp, color = ByceCoolGray)
                            }
                        }
                        Checkbox(
                            checked = applyCardCashback,
                            onCheckedChange = { applyCardCashback = it },
                            colors = CheckboxDefaults.colors(checkedColor = StatusInfoBlue)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ----------------------------------------------------
            // STEP 4: 30-DAY GRACE START WINDOW & CHECKOUT
            // ----------------------------------------------------
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "ORDER SUMMARY", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = TextSubtle, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${selectedPlan.name} ($selectedDuration)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${finalPrice.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonGreen
                            )
                            if (corpDiscount > 0 || cardCashback > 0) {
                                Text(
                                    text = "Total savings: ₹${(corpDiscount + cardCashback).toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusActiveGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 30-Day Flexible Start Window
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x20FFFFFF))
                            .clickable { showGraceStartModal = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedGraceDays == 0) "Starts Immediately (Today)" else "Starts in $selectedGraceDays days (30-day grace)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }
                        Text(text = "Change", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instant Checkout Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF6B9330), Color(0xFF4E7320))
                                )
                            )
                            .clickable {
                                lastPurchasedPlan = selectedPlan
                                showPurchaseSuccessModal = true
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pay ₹${finalPrice.toInt()} & Activate Tier",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Bottom Nav Bar
        GlassBottomBar(
            selectedTab = "Membership",
            isScrolling = scrollState.isScrollInProgress,
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // ----------------------------------------------------
        // MODALS
        // ----------------------------------------------------

        // 1. Dynamic Rotating QR Code Modal
        if (showQrPassModal) {
            DynamicQrPassDialog(onDismiss = { showQrPassModal = false })
        }

        // 2. Membership Pause / Freeze Modal
        if (showPauseModal) {
            MembershipPauseDialog(
                planName = selectedPlan.name,
                allowedDays = selectedPlan.pauseDaysAllowed,
                onDismiss = { showPauseModal = false }
            )
        }

        // 3. 30-Day Grace Start Date Modal
        if (showGraceStartModal) {
            GraceStartWindowDialog(
                selectedDays = selectedGraceDays,
                onSelectDays = {
                    selectedGraceDays = it
                    showGraceStartModal = false
                },
                onDismiss = { showGraceStartModal = false }
            )
        }

        // 4. Purchase Success Modal
        if (showPurchaseSuccessModal) {
            PurchaseSuccessDialog(
                plan = lastPurchasedPlan ?: selectedPlan,
                finalPrice = finalPrice,
                graceDays = selectedGraceDays,
                onViewPass = {
                    showPurchaseSuccessModal = false
                    showQrPassModal = true
                },
                onDismiss = { showPurchaseSuccessModal = false }
            )
        }
    }
}

// ----------------------------------------------------
// SUB-COMPONENTS
// ----------------------------------------------------

@Composable
private fun TrialSessionRow(
    session: FreeTrialSession,
    canBook: Boolean,
    onBook: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x18FFFFFF))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.sessionType,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x25FFFFFF))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = session.intensity, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ByceCoolGray)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${session.date} · ${session.timeSlot} · ${session.trainerName}",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        if (session.status == "Booked") {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(StatusActiveGreenBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "Booked ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusActiveGreen)
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (canBook) StatusActiveGreenBg else Color(0x20FFFFFF))
                    .clickable(enabled = canBook) { onBook() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Claim Slot",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canBook) StatusActiveGreen else TextMuted
                )
            }
        }
    }
}

@Composable
private fun CultTierCard(
    plan: MembershipPlanItem,
    duration: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val displayPrice = when (duration) {
        "1 Month" -> plan.monthlyPrice
        "3 Months (15% OFF)" -> plan.quarterlyPrice
        else -> plan.annualPrice
    }

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isSelected) Color(0x35FFFFFF) else Color.Transparent)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = plan.tierLevel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) NeonGreen else TextSubtle,
                            letterSpacing = 0.5.sp
                        )
                        if (plan.isPopular) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusActiveGreenBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "POPULAR", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = StatusActiveGreen)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plan.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plan.description,
                        fontSize = 11.sp,
                        color = ByceCoolGray
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${displayPrice.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) NeonGreen else TextWhite
                    )
                    Text(
                        text = when (duration) {
                            "1 Month" -> "/ month"
                            "3 Months (15% OFF)" -> "/ 3 months"
                            else -> "/ year"
                        },
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            plan.features.forEach { feat ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (isSelected) NeonGreen else ByceGreen, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = feat, fontSize = 11.sp, color = TextWhite)
                }
            }
        }
    }
}

// ----------------------------------------------------
// DIALOGS
// ----------------------------------------------------

@Composable
private fun DynamicQrPassDialog(onDismiss: () -> Unit) {
    var secondsLeft by remember { mutableStateOf(30) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
            if (secondsLeft == 0) {
                secondsLeft = 30 // auto rotate dynamic TOTP
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "DYNAMIC ENTRY PASS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TextSubtle, letterSpacing = 1.sp)
                        Text(text = "Scan at Turnstile / Greeter", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated Dynamic QR Box
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QrCode2, contentDescription = "QR", tint = DarkNavy, modifier = Modifier.size(120.dp))
                        Text(text = "BYCE-TOTP-SECURE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Countdown Timer Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Regenerates in ${secondsLeft}s (Anti-screenshot security)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x20FFFFFF))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "First-time trialists receive a 5-min facility tour upon scanning.",
                        fontSize = 11.sp,
                        color = ByceCoolGray
                    )
                }
            }
        }
    }
}

@Composable
private fun MembershipPauseDialog(
    planName: String,
    allowedDays: Int,
    onDismiss: () -> Unit
) {
    var pauseDays by remember { mutableStateOf(14) }
    var pauseReason by remember { mutableStateOf("Travel / Vacation") }

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Pause Membership", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Freezing your $planName extends your expiry date without losing membership validity.",
                    fontSize = 11.sp,
                    color = ByceCoolGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Select Pause Duration (Max $allowedDays Days)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(7, 14, 21, 30).filter { it <= allowedDays }.forEach { days ->
                        val isSelected = pauseDays == days
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0x40FFFFFF) else Color(0x18FFFFFF))
                                .clickable { pauseDays = days }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$days Days", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) TextWhite else TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6B9330), Color(0xFF4E7320))
                            )
                        )
                        .clickable {
                            GymOwnerRepository.requestMembershipFreeze(
                                planName = planName,
                                startDate = "Tomorrow",
                                endDate = "In $pauseDays days",
                                days = pauseDays,
                                reason = pauseReason
                            )
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Confirm $pauseDays-Day Pause", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }
        }
    }
}

@Composable
private fun GraceStartWindowDialog(
    selectedDays: Int,
    onSelectDays: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Flexible Start Date", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Lock in your plan discount today and start anytime within the 30-day grace period.",
                    fontSize = 11.sp,
                    color = ByceCoolGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                listOf(
                    Pair(0, "Start Immediately (Today)"),
                    Pair(7, "Start in 7 Days"),
                    Pair(14, "Start in 14 Days"),
                    Pair(30, "Start in 30 Days (Max Grace Window)")
                ).forEach { (days, label) ->
                    val isSelected = selectedDays == days
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x35FFFFFF) else Color(0x15FFFFFF))
                            .clickable { onSelectDays(days) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun PurchaseSuccessDialog(
    plan: MembershipPlanItem,
    finalPrice: Double,
    graceDays: Int,
    onViewPass: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(StatusActiveGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusActiveGreen, modifier = Modifier.size(30.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Membership Activated!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${plan.name} (${plan.tierLevel}) is ready to use.",
                    fontSize = 12.sp,
                    color = ByceCoolGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x20FFFFFF))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Amount Paid", fontSize = 11.sp, color = TextMuted)
                            Text(text = "₹${finalPrice.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Start Window", fontSize = 11.sp, color = TextMuted)
                            Text(text = if (graceDays == 0) "Immediate" else "In $graceDays days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Daily Entry", fontSize = 11.sp, color = TextMuted)
                            Text(text = "Dynamic Turnstile QR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6B9330), Color(0xFF4E7320))
                            )
                        )
                        .clickable { onViewPass() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "View Dynamic Entry Pass", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }
        }
    }
}
