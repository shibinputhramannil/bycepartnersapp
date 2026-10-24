package com.example

import com.example.ui.owner.GymTab
import com.example.ui.owner.OwnerScreenRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GymTabRoutingTest {

  @Test
  fun gym_screen_tabs_have_stable_labels() {
    assertEquals(listOf("Profile", "Location & Map", "Check-in QR"), GymTab.entries.map { it.label })
  }

  @Test
  fun location_is_a_tab_of_gym_profile_not_a_separate_route() {
    assertFalse(OwnerScreenRoute.entries.any { it.title == "Gym Location" })
  }
}
