package com.example.ui.auth

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.services.DemoPhoneAuthService
import com.example.data.services.OtpRequestResult
import com.example.data.services.OtpVerifyResult
import com.example.data.services.PhoneAuthService
import com.example.data.services.PhoneNumber
import com.example.data.services.PhoneValidation
import com.example.ui.components.FloatingBlurBalls
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.theme.ByceGreen
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DarkNavyDepth
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderSpecular
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AuthStep {
    PHONE,
    OTP,
    SUCCESS
}

private data class LoginSlide(@param:DrawableRes val imageRes: Int, val title: String, val caption: String)

// CC0 photos from Wikimedia Commons; sources are listed in the README
private val loginSlides = listOf(
    LoginSlide(R.drawable.login_slide_barbell, "Every rep counts", "Track check-ins as members walk in."),
    LoginSlide(R.drawable.login_slide_gym_floor, "Built for busy floors", "Attendance and bookings at a glance."),
    LoginSlide(R.drawable.login_slide_dumbbells, "Strength, every day", "Keep your members moving with BYCE.")
)

private const val SLIDE_INTERVAL_MS = 3500L
private const val SUCCESS_HOLD_MS = 1500L

@Composable
fun AuthScreen(
    onAuthComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
    authService: PhoneAuthService = remember { DemoPhoneAuthService() },
    autoScrollCarousel: Boolean = true
) {
    var step by rememberSaveable { mutableStateOf(AuthStep.PHONE) }
    var countryIndex by rememberSaveable { mutableStateOf(0) }
    var phoneDigits by rememberSaveable { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var otpCode by rememberSaveable { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val country = PhoneValidation.countryCodes[countryIndex]
    val phone = PhoneNumber(country, phoneDigits)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = step == AuthStep.OTP) {
        step = AuthStep.PHONE
        otpCode = ""
        otpError = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .testTag("user_auth_screen_container")
    ) {
        FloatingBlurBalls()

        when (step) {
            AuthStep.PHONE -> PhoneLoginStep(
                countryIndex = countryIndex,
                onCountrySelected = {
                    countryIndex = it
                    phoneDigits = PhoneValidation.sanitize(phoneDigits, PhoneValidation.countryCodes[it])
                    phoneError = null
                },
                phoneDigits = phoneDigits,
                onPhoneChange = {
                    phoneDigits = PhoneValidation.sanitize(it, country)
                    phoneError = null
                },
                phoneError = phoneError,
                isDemo = authService.isDemo,
                autoScrollCarousel = autoScrollCarousel,
                onContinue = {
                    val error = PhoneValidation.validate(phoneDigits, country)
                    if (error != null) {
                        phoneError = error
                    } else if (!isSubmitting) {
                        isSubmitting = true
                        scope.launch {
                            when (val result = authService.requestOtp(phone)) {
                                OtpRequestResult.CodeRequested -> {
                                    otpCode = ""
                                    otpError = null
                                    step = AuthStep.OTP
                                }
                                is OtpRequestResult.Failed -> phoneError = result.message
                            }
                            isSubmitting = false
                        }
                    }
                }
            )

            AuthStep.OTP -> OtpStep(
                phoneDisplay = phone.display,
                otpCode = otpCode,
                onOtpChange = {
                    otpCode = it.filter { c -> c.isDigit() }.take(PhoneValidation.OTP_LENGTH)
                    otpError = null
                },
                otpError = otpError,
                isDemo = authService.isDemo,
                onEditNumber = {
                    step = AuthStep.PHONE
                    otpCode = ""
                    otpError = null
                },
                onVerify = {
                    if (!PhoneValidation.isValidOtp(otpCode)) {
                        otpError = "Enter the 6-digit code."
                    } else if (!isSubmitting) {
                        isSubmitting = true
                        scope.launch {
                            when (val result = authService.verifyOtp(phone, otpCode)) {
                                OtpVerifyResult.Verified -> step = AuthStep.SUCCESS
                                is OtpVerifyResult.Rejected -> otpError = result.message
                            }
                            isSubmitting = false
                        }
                    }
                }
            )

            AuthStep.SUCCESS -> LoginSuccessStep(onContinue = onAuthComplete)
        }
    }
}

@Composable
private fun PhoneLoginStep(
    countryIndex: Int,
    onCountrySelected: (Int) -> Unit,
    phoneDigits: String,
    onPhoneChange: (String) -> Unit,
    phoneError: String?,
    isDemo: Boolean,
    autoScrollCarousel: Boolean,
    onContinue: () -> Unit
) {
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .testTag("login_phone_step")
    ) {
        FitnessCarousel(
            autoScroll = autoScrollCarousel,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Matches the carousel's bottom gradient stop so there's no seam
                .background(DarkNavy)
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = bottomPadding + 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PortalBadge()
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Log in to your gym",
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextWhite,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage Iron House Fitness · Kozhikode",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Phone number",
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                CountryCodeSelector(
                    selectedIndex = countryIndex,
                    onSelected = onCountrySelected
                )
                Spacer(modifier = Modifier.width(8.dp))
                LiquidGlassTextField(
                    value = phoneDigits,
                    onValueChange = onPhoneChange,
                    placeholder = "98470 12345",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    errorText = phoneError,
                    testTag = "login_phone_input",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LiquidGlassButton(
                text = "Continue",
                onClick = onContinue,
                testTag = "login_continue_button"
            )

            if (isDemo) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Demo login · No SMS is sent",
                    fontSize = 12.sp,
                    color = TextSubtle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("login_demo_notice")
                )
            }
        }
    }
}

@Composable
private fun FitnessCarousel(
    autoScroll: Boolean,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { loginSlides.size })
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Restarts after every page change, so a manual swipe also resets the timer
    LaunchedEffect(autoScroll, pagerState.currentPage) {
        if (!autoScroll) return@LaunchedEffect
        delay(SLIDE_INTERVAL_MS)
        pagerState.animateScrollToPage((pagerState.currentPage + 1) % loginSlides.size)
    }

    Box(modifier = modifier.testTag("login_carousel")) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val slide = loginSlides[page]
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = slide.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dark gradient: keeps the logo readable at the top and fades into the form below
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to DarkNavyDepth.copy(alpha = 0.70f),
                                0.35f to Color.Transparent,
                                0.65f to DarkNavy.copy(alpha = 0.55f),
                                1f to DarkNavy
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, end = 24.dp, bottom = 36.dp)
                ) {
                    Text(
                        text = slide.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = slide.caption,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.byce_logo),
            contentDescription = "BYCE Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topPadding + 16.dp)
                .height(30.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(loginSlides.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(if (selected) 18.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) TextWhite else Color(0x55FFFFFF))
                )
            }
        }
    }
}

@Composable
private fun CountryCodeSelector(
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = PhoneValidation.countryCodes[selectedIndex]

    Box {
        // Same glass treatment as LiquidGlassTextField so the pair reads as one control
        Row(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.verticalGradient(listOf(Color(0x20FFFFFF), Color(0x12FFFFFF))))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(GlassBorderSpecular, GlassBorderLight, Color(0x15FFFFFF))),
                    shape = RoundedCornerShape(22.dp)
                )
                .clickable { expanded = true }
                .padding(start = 14.dp, end = 8.dp)
                .testTag("login_country_code"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected.dialCode,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextWhite
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Choose country code",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkNavyDepth)
        ) {
            PhoneValidation.countryCodes.forEachIndexed { index, code ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${code.isoCode}  ${code.dialCode}",
                            fontSize = 14.sp,
                            color = TextWhite
                        )
                    },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PortalBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x28FFFFFF))
            .border(1.dp, GlassBorderSpecular, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = "GYM OWNER PORTAL",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextWhite,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun OtpStep(
    phoneDisplay: String,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    otpError: String?,
    isDemo: Boolean,
    onEditNumber: () -> Unit,
    onVerify: () -> Unit
) {
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(top = topPadding + 12.dp, bottom = bottomPadding + 16.dp)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .testTag("login_otp_step"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onEditNumber)
                .padding(vertical = 8.dp)
                .testTag("otp_back_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to phone number",
                tint = TextWhite,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Image(
            painter = painterResource(id = R.drawable.byce_logo),
            contentDescription = "BYCE Logo",
            modifier = Modifier.height(30.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        PortalBadge()
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Enter verification code",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextWhite,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Enter the 6-digit code for $phoneDisplay",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )

        if (isDemo) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x18FFFFFF))
                    .border(1.dp, GlassBorderSpecular, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("otp_demo_notice")
            ) {
                Text(
                    text = "Demo mode: no SMS was sent. Enter ${DemoPhoneAuthService.DEMO_CODE} to continue.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // One hidden text field drives the six glass cells
        BasicTextField(
            value = otpCode,
            onValueChange = onOtpChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .focusRequester(focusRequester)
                .testTag("otp_input"),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(PhoneValidation.OTP_LENGTH) { cellIndex ->
                        val char = otpCode.getOrNull(cellIndex)?.toString() ?: ""
                        val isActive = cellIndex == otpCode.length
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x20FFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = when {
                                        otpError != null -> Color(0xFFFF6B6B)
                                        char.isNotEmpty() || isActive -> ByceGreen.copy(alpha = 0.6f)
                                        else -> Color(0x30FFFFFF)
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.ifEmpty { "•" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = if (char.isNotEmpty()) TextWhite else TextSubtle
                            )
                        }
                    }
                }
            }
        )

        if (otpError != null) {
            Text(
                text = otpError,
                fontSize = 13.sp,
                color = Color(0xFFFF6B6B),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("otp_error")
            )
        }

        LiquidGlassButton(
            text = "Verify and log in",
            onClick = onVerify,
            testTag = "verify_otp_button"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Change phone number",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ByceGreen,
            modifier = Modifier
                .clickable(onClick = onEditNumber)
                .padding(vertical = 4.dp)
                .testTag("otp_change_number")
        )
    }
}

@Composable
private fun LoginSuccessStep(onContinue: () -> Unit) {
    var hasContinued by remember { mutableStateOf(false) }
    val continueOnce = {
        if (!hasContinued) {
            hasContinued = true
            onContinue()
        }
    }

    LaunchedEffect(Unit) {
        delay(SUCCESS_HOLD_MS)
        continueOnce()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .testTag("login_success_step"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0x20FFFFFF))
                .border(1.dp, NeonGreen.copy(alpha = 0.40f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Login successful",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Opening your partner dashboard…",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        LiquidGlassButton(
            text = "Go to dashboard",
            onClick = continueOnce,
            testTag = "login_success_continue_button"
        )
    }
}
