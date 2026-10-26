package com.example

import android.Manifest
import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.ui.owner.OwnerBookingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class BookingRemindersUiTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bookings_screen_has_no_gmail_reminders_and_says_reminders_are_for_staff_on_this_device() {
    composeTestRule.setContent { MyApplicationTheme { OwnerBookingsScreen() } }

    composeTestRule.onNodeWithText("STAFF REMINDERS").assertExists()
    composeTestRule.onNodeWithText("Alerts on this device").assertExists()
    composeTestRule.onNodeWithText("members aren't notified", substring = true).assertExists()
    composeTestRule.onNodeWithText("sample data and reset when the app restarts", substring = true).assertExists()

    // Case-sensitive: member email addresses such as "...@gmail.com" are still shown
    composeTestRule.onAllNodesWithText("Gmail", substring = true).assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Open Gmail draft").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Reminder sent", substring = true).assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Suggested:", substring = true).assertCountEquals(0)
  }

  @Test
  fun asks_for_notification_permission_when_it_is_missing() {
    shadowOf(ApplicationProvider.getApplicationContext<Application>()).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
    composeTestRule.setContent { MyApplicationTheme { OwnerBookingsScreen() } }

    composeTestRule.onNodeWithText("Off").assertExists()
    composeTestRule.onNodeWithText("Notifications are off for BYCE Partner, so reminders won't appear.").assertExists()
    composeTestRule.onNodeWithText("Allow notifications").assertExists()
  }

  @Test
  fun shows_reminders_on_when_permission_is_granted() {
    shadowOf(ApplicationProvider.getApplicationContext<Application>()).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    composeTestRule.setContent { MyApplicationTheme { OwnerBookingsScreen() } }

    composeTestRule.onNodeWithText("On").assertExists()
    composeTestRule.onAllNodesWithText("Allow notifications").assertCountEquals(0)
  }
}
