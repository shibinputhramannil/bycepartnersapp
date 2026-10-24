package com.example

import com.example.ui.owner.GymTab
import com.example.ui.owner.OwnerScreenRoute
import com.example.ui.owner.gymTabFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GymTabRoutingTest {

  @Test
  fun gym_location_route_opens_the_location_tab() {
    assertEquals(GymTab.LOCATION, gymTabFor(OwnerScreenRoute.GYM_LOCATION))
    assertEquals("Location & Map", GymTab.LOCATION.label)
  }

  @Test
  fun gym_profile_route_opens_the_profile_tab() {
    assertEquals(GymTab.PROFILE, gymTabFor(OwnerScreenRoute.GYM_PROFILE))
  }

  @Test
  fun non_gym_routes_have_no_gym_tab() {
    assertNull(gymTabFor(OwnerScreenRoute.DASHBOARD))
    assertNull(gymTabFor(OwnerScreenRoute.PAYMENTS))
  }
}
