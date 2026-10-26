package com.example

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.data.services.DemoPhoneAuthService
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// LiquidGlassTextField puts its test tag on the box around the editable field
internal fun SemanticsNodeInteractionsProvider.phoneField() =
  onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("login_phone_input")))

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class LoginFlowUiTest {

  @get:Rule val composeTestRule = createComposeRule()

  private var authCompleteCount = 0

  private fun setLoginScreen() {
    composeTestRule.setContent {
      MyApplicationTheme {
        AuthScreen(onAuthComplete = { authCompleteCount++ }, autoScrollCarousel = false)
      }
    }
  }

  private fun enterPhoneAndContinue(number: String) {
    composeTestRule.phoneField().performTextInput(number)
    composeTestRule.onNodeWithTag("login_continue_button").performClick()
  }

  @Test
  fun login_screen_has_carousel_phone_field_and_no_password_or_sign_up() {
    setLoginScreen()

    composeTestRule.onNodeWithTag("login_carousel").assertExists()
    composeTestRule.onNodeWithText("Every rep counts").assertExists()
    composeTestRule.onNodeWithTag("login_country_code").assertExists()
    composeTestRule.onNodeWithText("+91").assertExists()
    composeTestRule.onNodeWithTag("login_continue_button").assertExists()
    composeTestRule.onNodeWithText("Demo login · No SMS is sent").assertExists()

    composeTestRule.onAllNodesWithText("Password").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Sign up").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Forgot password?").assertCountEquals(0)
  }

  @Test
  fun invalid_phone_shows_error_and_stays_on_login() {
    setLoginScreen()
    enterPhoneAndContinue("12345")

    composeTestRule.onNodeWithText("Enter a 10-digit phone number.").assertExists()
    composeTestRule.onAllNodesWithTag("login_otp_step").assertCountEquals(0)
  }

  @Test
  fun empty_phone_shows_error() {
    setLoginScreen()
    composeTestRule.onNodeWithTag("login_continue_button").performClick()

    composeTestRule.onNodeWithText("Enter your phone number.").assertExists()
    composeTestRule.onAllNodesWithTag("login_otp_step").assertCountEquals(0)
  }

  @Test
  fun valid_phone_opens_otp_screen_and_back_keeps_number() {
    setLoginScreen()
    enterPhoneAndContinue("9847012345")

    composeTestRule.onNodeWithTag("login_otp_step").assertExists()
    composeTestRule.onNodeWithText("Enter the 6-digit code for +91 9847012345").assertExists()
    composeTestRule.onNodeWithTag("otp_demo_notice").assertExists()
    composeTestRule.onAllNodesWithText("sent a 6-digit code", substring = true).assertCountEquals(0)

    composeTestRule.onNodeWithTag("otp_back_button").performClick()
    composeTestRule.onNodeWithTag("login_phone_step").assertExists()
    composeTestRule.onNodeWithText("9847012345").assertExists()

    composeTestRule.onNodeWithTag("login_continue_button").performClick()
    composeTestRule.onNodeWithTag("otp_change_number").performClick()
    composeTestRule.onNodeWithTag("login_phone_step").assertExists()
  }

  @Test
  fun wrong_otp_shows_error_and_does_not_log_in() {
    setLoginScreen()
    enterPhoneAndContinue("9847012345")

    composeTestRule.onNodeWithTag("otp_input").performTextInput("654321")
    composeTestRule.onNodeWithTag("verify_otp_button").performClick()

    composeTestRule.onNodeWithText("That code isn't correct. Check it and try again.").assertExists()
    composeTestRule.onAllNodesWithText("Login successful").assertCountEquals(0)
    assertEquals(0, authCompleteCount)
  }

  @Test
  fun demo_otp_shows_login_successful_then_completes() {
    setLoginScreen()
    enterPhoneAndContinue("9847012345")

    composeTestRule.onNodeWithTag("otp_input").performTextInput(DemoPhoneAuthService.DEMO_CODE)
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.onNodeWithTag("verify_otp_button").performClick()
    composeTestRule.mainClock.advanceTimeByFrame()
    composeTestRule.mainClock.advanceTimeByFrame()

    composeTestRule.onNodeWithText("Login successful").assertIsDisplayed()
    assertEquals(0, authCompleteCount)

    composeTestRule.mainClock.advanceTimeBy(2_000)
    composeTestRule.waitForIdle()
    assertEquals(1, authCompleteCount)

    // Tapping the button after the automatic hand-off doesn't log in twice
    composeTestRule.mainClock.autoAdvance = true
    composeTestRule.onNodeWithTag("login_success_continue_button").performClick()
    assertEquals(1, authCompleteCount)
  }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AppStartupUiTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun app_opens_on_login_screen_not_dashboard() {
    composeTestRule.onNodeWithTag("user_auth_screen_container").assertExists()
    composeTestRule.onNodeWithTag("login_phone_step").assertExists()
    composeTestRule.onAllNodesWithText("WELCOME BACK").assertCountEquals(0)
  }

  @Test
  fun demo_login_reaches_dashboard() {
    composeTestRule.phoneField().performTextInput("9847012345")
    composeTestRule.onNodeWithTag("login_continue_button").performClick()
    composeTestRule.onNodeWithTag("otp_input").performTextInput(DemoPhoneAuthService.DEMO_CODE)
    composeTestRule.onNodeWithTag("verify_otp_button").performClick()

    composeTestRule.mainClock.advanceTimeBy(3_000)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("WELCOME BACK").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onAllNodesWithTag("user_auth_screen_container").assertCountEquals(0)
  }
}
