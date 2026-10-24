package com.example

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.components.OwnerNavigationDrawerSheet
import com.example.ui.owner.OwnerAttendanceScreen
import com.example.ui.owner.OwnerGymScreen
import com.example.ui.owner.OwnerSettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Checks that removed or relabelled prototype UI stays gone on the real screens.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class PartnerCleanupUiTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun drawer_has_no_reports_entry() {
    composeTestRule.setContent {
      MyApplicationTheme { OwnerNavigationDrawerSheet(currentScreenName = "Dashboard", onNavigate = {}, onClose = {}, onLogout = {}) }
    }
    composeTestRule.onNodeWithText("Attendance").assertExists()
    composeTestRule.onAllNodesWithText("Reports").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Gym Location").assertCountEquals(0)
  }

  @Test
  fun drawer_badges_show_passed_counts_and_hide_zero() {
    composeTestRule.setContent {
      MyApplicationTheme {
        OwnerNavigationDrawerSheet(
          currentScreenName = "Dashboard",
          onNavigate = {},
          onClose = {},
          onLogout = {},
          todayCheckIns = 8,
          upcomingBookings = 0,
          unreadNotifications = 3
        )
      }
    }
    composeTestRule.onNodeWithText("8").assertExists()
    composeTestRule.onNodeWithText("3").assertExists()
    // The old hardcoded badges must not appear
    composeTestRule.onAllNodesWithText("86").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("42").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("0").assertCountEquals(0)
  }

  @Test
  fun settings_has_no_fake_toggles_or_two_factor_claim() {
    composeTestRule.setContent { MyApplicationTheme { OwnerSettingsScreen() } }
    composeTestRule.onAllNodesWithText("Alerts").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Gym").assertCountEquals(0)

    composeTestRule.onNodeWithText("Security").performClick()
    composeTestRule.onAllNodesWithText("Two-Factor Authentication").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Last updated 30 days ago").assertCountEquals(0)
  }

  @Test
  fun gym_profile_shows_address_and_coordinates_without_a_map_tab() {
    composeTestRule.setContent { MyApplicationTheme { OwnerGymScreen() } }
    composeTestRule.onNodeWithText("ADDRESS & PINCODE").assertExists()
    composeTestRule.onNodeWithText("Coordinates: 11.2588° N, 75.7804° E").assertExists()

    composeTestRule.onAllNodesWithText("Location & Map").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Map view", substring = true).assertCountEquals(0)
    composeTestRule.onAllNodesWithText("GPS Accuracy", substring = true).assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Satellite").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Discovery Status").assertCountEquals(0)
  }

  @Test
  fun attendance_has_no_fake_qr_pass_or_audio_claims() {
    composeTestRule.setContent { MyApplicationTheme { OwnerAttendanceScreen() } }
    // Positive anchors so the absence checks below can't pass on an empty screen
    composeTestRule.onNodeWithText("Avg Daily Visits").assertExists()
    composeTestRule.onNodeWithText("Trial Member Has Arrived").assertExists()

    composeTestRule.onAllNodesWithText("Audio Buzzer Alert Triggered").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("QR").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Tap for Graph").assertCountEquals(0)
  }
}
