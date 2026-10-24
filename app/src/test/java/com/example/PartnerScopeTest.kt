package com.example

import com.example.data.services.GymOwnerRepository
import com.example.ui.owner.OwnerScreenRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PartnerScopeTest {

  private val repo = GymOwnerRepository

  @Test
  fun owner_routes_are_the_core_partner_workflows() {
    val expected = setOf(
      "DASHBOARD", "ATTENDANCE", "CUSTOMERS", "MEMBERSHIPS", "PLANS", "PAYMENTS", "BOOKINGS",
      "GYM_PROFILE", "GYM_LOCATION", "STAFF", "ACTIVITY_LOG", "NOTIFICATIONS", "SETTINGS"
    )
    assertEquals(expected, OwnerScreenRoute.entries.map { it.name }.toSet())
  }

  @Test
  fun member_without_recorded_events_has_an_empty_timeline() {
    // c8 Vivek Raj has no sample activity events
    assertTrue(repo.getCustomerActivityTimeline("c8").isEmpty())
    assertFalse(repo.getCustomerActivityTimeline("c1").isEmpty())
  }

  @Test
  fun last_check_in_comes_from_real_attendance_records() {
    // c7 Pooja Krishnan's only record is from yesterday
    val visit = repo.getLastCheckIn("c7")
    assertEquals("Yesterday", visit?.date)
    assertEquals("05:15 PM", visit?.checkInTime)
    assertNull(repo.getLastCheckIn("no-such-member"))
  }

  @Test
  fun trial_alert_does_not_claim_turnstile_hardware() {
    val alert = repo.getActiveTrialBuzzer() ?: return
    assertFalse(alert.arrivalTime.contains("Turnstile", ignoreCase = true))
  }
}
