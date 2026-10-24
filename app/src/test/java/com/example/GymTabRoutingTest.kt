package com.example

import com.example.ui.owner.GymTab
import com.example.ui.owner.OwnerScreenRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GymTabRoutingTest {

  @Test
  fun gym_screen_has_profile_and_check_in_qr_tabs_only() {
    assertEquals(listOf("Profile", "Check-in QR"), GymTab.entries.map { it.label })
  }

  @Test
  fun there_is_no_separate_location_route() {
    assertFalse(OwnerScreenRoute.entries.any { it.title == "Gym Location" })
  }
}
