package com.example.ui.auth

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import com.example.ui.theme.GlassBorderSpecular
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FloatingBlurBalls
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassSegmentedControl
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.theme.ByceGreen
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DarkNavyDepth
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

enum class AuthMode {
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    VERIFICATION,
    VERIFICATION_SUCCESS
}

@Composable
fun AuthScreen(
    onAuthComplete: () -> Unit = {},
    onBackToOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Login State
    var loginPhone by remember { mutableStateOf("+91 98470 12345") }
    var loginPassword by remember { mutableStateOf("byce1234") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Sign Up State
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }
    var signUpError by remember { mutableStateOf<String?>(null) }

    // Forgot Password State
    var forgotPhone by remember { mutableStateOf("") }
    var forgotStep by remember { mutableIntStateOf(1) } // 1 = Phone, 2 = Code, 3 = New Password
    var newPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }

    // OTP Verification State
    var otpCode by remember { mutableStateOf(listOf("", "", "", "", "", "")) }
    var otpTimerSeconds by remember { mutableIntStateOf(42) }
    var otpError by remember { mutableStateOf<String?>(null) }

    // Timer countdown for OTP resend
    LaunchedEffect(authMode, otpTimerSeconds) {
        if (authMode == AuthMode.VERIFICATION && otpTimerSeconds > 0) {
            delay(1000L)
            otpTimerSeconds--
        }
    }

    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .testTag("user_auth_screen_container")
    ) {
        // Cult.fit Aurora Animated Ambient Lighting
        FloatingBlurBalls()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding + 12.dp, bottom = bottomPadding + 16.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --------------------------------------------------
            // SEGMENTED SWITCH (Log in / Sign up) - Fixed at Top
            // --------------------------------------------------
            if (authMode == AuthMode.LOGIN || authMode == AuthMode.SIGNUP) {
                LiquidGlassSegmentedControl(
                    options = listOf("Log in", "Sign up"),
                    selectedIndex = if (authMode == AuthMode.LOGIN) 0 else 1,
                    onOptionSelected = { index ->
                        authMode = if (index == 0) AuthMode.LOGIN else AuthMode.SIGNUP
                        loginError = null
                        signUpError = null
                    },
                    modifier = Modifier.padding(bottom = 12.dp),
                    testTag = "user_auth_segmented_control"
                )
            }

            // --------------------------------------------------
            // FORM CONTENT CONTAINER - Centered Vertically
            // --------------------------------------------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // HEADER TYPOGRAPHY
                    val headlineText = when (authMode) {
                        AuthMode.LOGIN -> "Welcome back"
                        AuthMode.SIGNUP -> "Create account"
                        AuthMode.FORGOT_PASSWORD -> "Reset your password"
                        AuthMode.VERIFICATION -> "Verify your phone"
                        AuthMode.VERIFICATION_SUCCESS -> "You're all set"
                    }
                    val supportingText = when (authMode) {
                        AuthMode.LOGIN -> ""
                        AuthMode.SIGNUP -> ""
                        AuthMode.FORGOT_PASSWORD -> "Enter the phone number associated with your Byce account."
                        AuthMode.VERIFICATION -> "We sent a 6-digit code to your phone number."
                        AuthMode.VERIFICATION_SUCCESS -> "Your Byce account is ready."
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.byce_logo),
                            contentDescription = "BYCE Logo",
                            modifier = Modifier.height(30.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = headlineText,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TextWhite,
                            lineHeight = 28.sp,
                            textAlign = TextAlign.Center
                        )
                        if (supportingText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = supportingText,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                color = TextMuted,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        } else if (authMode == AuthMode.LOGIN) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Manage Iron House Fitness · Kozhikode",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }


            // --------------------------------------------------
            // AUTH SCREEN STATES
            // --------------------------------------------------
            when (authMode) {
                AuthMode.LOGIN -> {
                    // LOGIN FORM STATE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LiquidGlassTextField(
                            value = loginPhone,
                            onValueChange = {
                                loginPhone = it
                                loginError = null
                            },
                            label = "Phone number",
                            placeholder = "+1 (555) 000-0000",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            errorText = loginError,
                            testTag = "login_phone_input"
                        )

                        Column {
                            LiquidGlassTextField(
                                value = loginPassword,
                                onValueChange = {
                                    loginPassword = it
                                    loginError = null
                                },
                                label = "Password",
                                placeholder = "Enter your password",
                                isPassword = true,
                                isPasswordVisible = loginPasswordVisible,
                                onPasswordToggleClick = { loginPasswordVisible = !loginPasswordVisible },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                testTag = "login_password_input"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Forgot password?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = ByceGreen,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { authMode = AuthMode.FORGOT_PASSWORD }
                                    .padding(vertical = 4.dp)
                                    .testTag("forgot_password_link")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LiquidGlassButton(
                            text = "Log in",
                            onClick = {
                                if (loginPhone.isEmpty()) {
                                    loginError = "Enter a valid phone number."
                                } else if (loginPassword.length < 6) {
                                    loginError = "Phone number or password is incorrect."
                                } else {
                                    onAuthComplete()
                                }
                            },
                            testTag = "login_primary_button"
                        )
                    }
                }

                AuthMode.SIGNUP -> {
                    // MEMBER SIGN UP FORM STATE (Clean, no password requirement box)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LiquidGlassTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = "Full name",
                            placeholder = "John Doe",
                            testTag = "signup_name_input"
                        )

                        LiquidGlassTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                signUpError = null
                            },
                            label = "Phone number",
                            placeholder = "+1 (555) 000-0000",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            errorText = signUpError,
                            testTag = "signup_phone_input"
                        )

                        LiquidGlassTextField(
                            value = signUpPassword,
                            onValueChange = { signUpPassword = it },
                            label = "Password",
                            placeholder = "Enter your password",
                            isPassword = true,
                            isPasswordVisible = signUpPasswordVisible,
                            onPasswordToggleClick = { signUpPasswordVisible = !signUpPasswordVisible },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            testTag = "signup_password_input"
                        )

                        LiquidGlassTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Confirm password",
                            placeholder = "Re-enter password",
                            isPassword = true,
                            isPasswordVisible = signUpPasswordVisible,
                            onPasswordToggleClick = { signUpPasswordVisible = !signUpPasswordVisible },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            testTag = "signup_confirm_password_input"
                        )

                        // Terms text link (Single AnnotatedString for perfect baseline and word spacing alignment)
                        val termsText = buildAnnotatedString {
                            withStyle(SpanStyle(color = TextSubtle, fontSize = 12.sp)) {
                                append("By continuing, you agree to the Byce ")
                            }
                            withStyle(SpanStyle(color = ByceGreen, fontWeight = FontWeight.Medium, fontSize = 12.sp)) {
                                append("terms")
                            }
                            withStyle(SpanStyle(color = TextSubtle, fontSize = 12.sp)) {
                                append(" and ")
                            }
                            withStyle(SpanStyle(color = ByceGreen, fontWeight = FontWeight.Medium, fontSize = 12.sp)) {
                                append("privacy policy.")
                            }
                        }

                        Text(
                            text = termsText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LiquidGlassButton(
                            text = "Create account",
                            onClick = {
                                val passwordsMatch = signUpPassword.isNotEmpty() && signUpPassword == confirmPassword
                                if (phoneNumber.isEmpty()) {
                                    signUpError = "Enter a valid phone number."
                                } else if (!passwordsMatch) {
                                    signUpError = "Passwords don't match."
                                } else {
                                    authMode = AuthMode.VERIFICATION
                                }
                            },
                            testTag = "signup_primary_button"
                        )
                    }
                }

                AuthMode.VERIFICATION -> {
                    // OTP VERIFICATION STATE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 6 Rounded liquid-glass OTP fields
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(6) { cellIndex ->
                                val char = otpCode.getOrNull(cellIndex) ?: ""
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0x20FFFFFF))
                                        .border(
                                            width = 1.dp,
                                            color = if (char.isNotEmpty()) ByceGreen.copy(alpha = 0.6f) else Color(0x30FFFFFF),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            val newCode = otpCode.toMutableList()
                                            newCode[cellIndex] = ((cellIndex + 1) * 2 % 9 + 1).toString()
                                            otpCode = newCode
                                            otpError = null
                                        },
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

                        if (otpError != null) {
                            Text(
                                text = otpError!!,
                                fontSize = 13.sp,
                                color = Color(0xFFFF6B6B),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        LiquidGlassButton(
                            text = "Verify phone",
                            onClick = {
                                if (otpCode.any { it.isEmpty() }) {
                                    otpError = "That code isn't correct. Check it and try again."
                                } else {
                                    authMode = AuthMode.VERIFICATION_SUCCESS
                                }
                            },
                            testTag = "verify_phone_button"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Resend code ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (otpTimerSeconds == 0) TextWhite else TextMuted,
                                modifier = Modifier.clickable(enabled = otpTimerSeconds == 0) {
                                    otpTimerSeconds = 42
                                    otpError = null
                                }
                            )
                            if (otpTimerSeconds > 0) {
                                Text(
                                    text = "in 00:${if (otpTimerSeconds < 10) "0$otpTimerSeconds" else otpTimerSeconds}",
                                    fontSize = 14.sp,
                                    color = TextSubtle
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Change phone number",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ByceGreen,
                            modifier = Modifier.clickable { authMode = AuthMode.SIGNUP }
                        )
                    }
                }

                AuthMode.VERIFICATION_SUCCESS -> {
                    // ACCOUNT VERIFIED SUCCESS STATE
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0x20FFFFFF))
                                .border(1.dp, com.example.ui.theme.NeonGreen.copy(alpha = 0.40f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Verified",
                                tint = TextWhite,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        LiquidGlassButton(
                            text = "Continue",
                            onClick = onAuthComplete,
                            testTag = "account_verified_continue_button"
                        )
                    }
                }

                AuthMode.FORGOT_PASSWORD -> {
                    // FORGOT PASSWORD RECOVERY FLOW
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (forgotStep == 1) {
                            LiquidGlassTextField(
                                value = forgotPhone,
                                onValueChange = { forgotPhone = it },
                                label = "Phone number",
                                placeholder = "+1 (555) 000-0000",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                testTag = "forgot_phone_input"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LiquidGlassButton(
                                text = "Send verification code",
                                onClick = { forgotStep = 2 },
                                testTag = "send_reset_code_button"
                            )
                        } else if (forgotStep == 2) {
                            Text(
                                text = "Enter verification code",
                                fontSize = 14.sp,
                                color = TextMuted
                            )

                            LiquidGlassTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = "Create a new password",
                                placeholder = "Enter new password",
                                isPassword = true,
                                isPasswordVisible = newPasswordVisible,
                                onPasswordToggleClick = { newPasswordVisible = !newPasswordVisible },
                                testTag = "new_password_input"
                            )

                            LiquidGlassButton(
                                text = "Create new password",
                                onClick = { forgotStep = 3 },
                                testTag = "create_new_password_button"
                            )
                        } else {
                            // Step 3: Password updated confirmation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Password updated",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextWhite
                                )
                            }

                            LiquidGlassButton(
                                text = "Log in",
                                onClick = {
                                    authMode = AuthMode.LOGIN
                                    forgotStep = 1
                                },
                                testTag = "login_after_reset_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Back to login",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clickable {
                                    authMode = AuthMode.LOGIN
                                    forgotStep = 1
                                }
                        )
                    }
                }
            }
        }
    }
}
}
}

