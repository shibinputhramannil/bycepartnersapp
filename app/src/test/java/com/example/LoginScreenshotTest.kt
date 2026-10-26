package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class LoginScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun login_phone_and_otp_screenshots() {
    composeTestRule.setContent { MyApplicationTheme { AuthScreen(autoScrollCarousel = false) } }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/login_phone.png")

    composeTestRule.phoneField().performTextInput("9847012345")
    composeTestRule.onNodeWithTag("login_continue_button").performClick()
    composeTestRule.onNodeWithTag("otp_input").performTextInput("123")
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/login_otp.png")
  }
}
