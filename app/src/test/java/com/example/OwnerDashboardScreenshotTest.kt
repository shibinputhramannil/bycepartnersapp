package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.example.ui.owner.OwnerDashboardScreen
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
class OwnerDashboardScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun owner_dashboard_screenshot() {
    composeTestRule.setContent { MyApplicationTheme { OwnerDashboardScreen() } }

    composeTestRule.onNodeWithText("WELCOME BACK").assertIsDisplayed()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/owner_dashboard.png")
  }
}
