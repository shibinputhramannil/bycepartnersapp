package com.example

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.ui.components.OwnerNavigationDrawerSheet
import com.example.ui.owner.GymTab
import com.example.ui.owner.OwnerDashboardScreen
import com.example.ui.owner.OwnerGymScreen
import com.example.ui.owner.OwnerMembershipsScreen
import com.example.ui.owner.OwnerPaymentsScreen
import com.example.ui.owner.OwnerPlansScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * BYCE is an aggregator: it owns pass tiers, pricing, payments, pausing, and listings.
 * These check the partner screens show that information read-only, without admin tools.
 * Each test asserts something real is on screen before checking what's absent.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AggregatorScopeUiTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun accepted_passes_are_read_only_and_exclude_non_gym_passes() {
    composeTestRule.setContent { MyApplicationTheme { OwnerPlansScreen() } }
    composeTestRule.onNodeWithText("Accepted Passes").assertExists()
    composeTestRule.onNodeWithText("All-Access Elite").assertExists()

    composeTestRule.onAllNodesWithText("Digital / At-Home").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Coaching & Nutrition").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Create Plan").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Edit Plan").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Deactivate").assertCountEquals(0)
  }

  @Test
  fun payments_are_read_only_demo_data_without_pos_or_collection_tools() {
    composeTestRule.setContent { MyApplicationTheme { OwnerPaymentsScreen() } }
    composeTestRule.onNodeWithText("Members pay through BYCE. This view is read-only.").assertExists()
    composeTestRule.onNodeWithText("Demo data: these figures and entries are samples, not live BYCE payments.").assertExists()
    composeTestRule.onNodeWithText("demo transactions", substring = true).assertExists()
    composeTestRule.onAllNodesWithText("On-Site POS Desk").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("New POS").assertCountEquals(0)

    composeTestRule.onNode(hasText("Pending Dues (", substring = true) and hasClickAction()).performClick()
    composeTestRule.onNodeWithText("Arjun Prasad").assertExists()
    composeTestRule.onNodeWithText("demo pending dues", substring = true).assertExists()
    composeTestRule.onNodeWithText("Demo data: these figures and entries are samples, not live BYCE payments.").assertExists()
    composeTestRule.onAllNodesWithText("Record Paid").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Remind", substring = true).assertCountEquals(0)
  }

  @Test
  fun dashboard_sample_figures_are_labelled_as_demo_data() {
    composeTestRule.setContent { MyApplicationTheme { OwnerDashboardScreen() } }
    // Each sample section is present, and carries its own notice
    composeTestRule.onNodeWithText("TOTAL CUSTOMERS").assertExists() // KPI card titles are upper-cased
    composeTestRule.onNodeWithText("CURRENT GYM OCCUPANCY").assertExists()
    composeTestRule.onNodeWithText("Revenue Overview").assertExists()
    composeTestRule.onNodeWithText("Pending Payments & Dues").assertExists()
    composeTestRule.onNodeWithText("Recent Payments").assertExists()

    composeTestRule.onNodeWithText("Demo data—not live: sample figures for today.").assertExists()
    composeTestRule.onNodeWithText("Demo data—not live: sample KPI figures.").assertExists()
    composeTestRule.onNodeWithText("Demo data—not live: sample occupancy figures.").assertExists()
    composeTestRule.onNodeWithText("Demo data—not live: sample revenue chart.").assertExists()
    composeTestRule.onNodeWithText("Demo data—not live: sample membership counts.").assertExists()
    composeTestRule.onAllNodesWithText("Demo data, not live BYCE payments.").assertCountEquals(2)
  }

  @Test
  fun membership_detail_shows_status_without_freeze_controls() {
    composeTestRule.setContent { MyApplicationTheme { OwnerMembershipsScreen() } }
    composeTestRule.onNode(hasScrollToNodeAction() and hasAnyDescendant(hasText("MEMBERSHIP PASSES")))
      .performScrollToNode(hasText("Details"))
    composeTestRule.onAllNodesWithText("Details")[0].performClick()

    composeTestRule.onNodeWithText("Membership Details").assertExists()
    composeTestRule.onNodeWithText("Paused").assertExists()
    composeTestRule.onAllNodesWithText("Freeze / Defer").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Unfreeze").assertCountEquals(0)
  }

  @Test
  fun gym_listing_status_is_read_only() {
    composeTestRule.setContent { MyApplicationTheme { OwnerGymScreen() } }
    composeTestRule.onNodeWithText("BYCE Listing").assertExists()
    composeTestRule.onNodeWithText("Managed by BYCE. Contact BYCE support to change your listing.").assertExists()
    composeTestRule.onAllNodesWithText("Platform Visibility").assertCountEquals(0)
    composeTestRule.onAllNodesWithText("Super Admin", substring = true).assertCountEquals(0)
  }

  @Test
  fun location_tab_has_no_duplicate_discovery_status() {
    composeTestRule.setContent { MyApplicationTheme { OwnerGymScreen(initialTab = GymTab.LOCATION) } }
    composeTestRule.onNodeWithText("Coordinates & Address").assertExists()
    composeTestRule.onAllNodesWithText("Discovery Status").assertCountEquals(0)
  }

  @Test
  fun drawer_shows_accepted_passes_not_membership_plans() {
    composeTestRule.setContent {
      MyApplicationTheme { OwnerNavigationDrawerSheet(currentScreenName = "Dashboard", onNavigate = {}, onClose = {}, onLogout = {}) }
    }
    composeTestRule.onNodeWithText("Accepted Passes").assertExists()
    composeTestRule.onAllNodesWithText("Membership Plans").assertCountEquals(0)
  }
}
