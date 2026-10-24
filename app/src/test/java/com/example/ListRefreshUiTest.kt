package com.example

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.ui.owner.OwnerCustomersScreen
import com.example.ui.owner.OwnerPaymentsScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The repository returns the same mutable list on every call, so these check that
 * the screens' filtered lists recompute after an add / remove.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ListRefreshUiTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun added_customer_appears_without_changing_the_filter() {
    val newName = "Refresh Test Member"
    composeTestRule.setContent { MyApplicationTheme { OwnerCustomersScreen() } }

    composeTestRule.onNodeWithText("Add Customer").performClick()
    composeTestRule.onAllNodes(hasSetTextAction() and hasAnyAncestor(isDialog()))[0].performTextInput(newName)
    composeTestRule.onNodeWithText("Register Customer").performClick()
    composeTestRule.waitForIdle()

    // Scroll the main customer list (the one containing the header) to the new row
    composeTestRule.onNode(hasScrollToNodeAction() and hasAnyDescendant(hasText("Add Customer")))
      .performScrollToNode(hasText(newName))
    composeTestRule.onNodeWithText(newName).assertExists()
  }

  @Test
  fun recorded_pending_payment_leaves_the_pending_list() {
    composeTestRule.setContent { MyApplicationTheme { OwnerPaymentsScreen() } }

    // The section tab, not the "Pending Dues" summary label
    composeTestRule.onNode(hasText("Pending Dues (", substring = true) and hasClickAction()).performClick()
    composeTestRule.onAllNodesWithText("Arjun Prasad").assertCountEquals(1)

    composeTestRule.onAllNodesWithText("Record Paid")[0].performClick()
    composeTestRule.onNodeWithText("Mark Paid").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithText("Arjun Prasad").assertCountEquals(0)
  }
}
