package com.example

import com.example.data.models.CheckInEligibility
import com.example.data.models.CheckInOutcome
import com.example.data.models.CustomerItem
import com.example.data.models.MemberStatus
import com.example.data.models.ScannedQrType
import com.example.data.services.GymOwnerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * GymOwnerRepository is a shared in-memory object, so each test that changes state
 * uses its own sample member and the tests do not depend on run order.
 */
class CheckInRulesTest {

  private val repo = GymOwnerRepository

  @Test
  fun active_member_checks_in_once_then_duplicate_is_blocked() {
    // c8 Vivek Raj: Active, earlier visit today already checked out
    assertEquals(CheckInEligibility.ALLOWED, repo.evaluateMemberCheckIn("c8").eligibility)
    assertTrue(repo.checkIn(customerId = "c8", source = "test") is CheckInOutcome.Success)

    assertEquals(CheckInEligibility.BLOCKED, repo.evaluateMemberCheckIn("c8").eligibility)
    val again = repo.checkIn(customerId = "c8", reason = "Manager approved", source = "test")
    assertTrue("duplicate must be blocked even with a reason", again is CheckInOutcome.Rejected)
  }

  @Test
  fun expired_member_needs_a_reason_and_the_override_is_logged() {
    // c5 Sneha Joseph: Expired
    assertEquals(CheckInEligibility.NEEDS_REASON, repo.evaluateMemberCheckIn("c5").eligibility)
    assertTrue(repo.checkIn(customerId = "c5", source = "test") is CheckInOutcome.Rejected)

    val outcome = repo.checkIn(customerId = "c5", reason = "Renewing at front desk", source = "test")
    assertTrue(outcome is CheckInOutcome.Success)
    val log = repo.getAdminActivityLogs().first().action
    assertTrue(log, log.contains("Check-in override for Sneha Joseph") && log.contains("Reason: Renewing at front desk"))
  }

  @Test
  fun pending_member_needs_a_reason() {
    // c6 Arjun Prasad: Pending
    assertEquals(CheckInEligibility.NEEDS_REASON, repo.evaluateMemberCheckIn("c6").eligibility)
  }

  @Test
  fun frozen_member_is_blocked_unless_overridden_with_a_reason() {
    // Partners can't freeze a pass; a frozen status comes from BYCE, so add one directly
    repo.addCustomer(
      CustomerItem("t_frozen", "Test Frozen Member", "frozen.member@example.com", "+91 90000 00002",
        "Monthly", "01 Oct 2026", "31 Oct 2026", MemberStatus.FROZEN, "01 Oct 2026", "TF")
    )
    assertEquals(CheckInEligibility.BLOCKED_OVERRIDABLE, repo.evaluateMemberCheckIn("t_frozen").eligibility)
    assertTrue(repo.checkIn(customerId = "t_frozen", source = "test") is CheckInOutcome.Rejected)

    val override = repo.checkIn(customerId = "t_frozen", reason = "Member ending pause early", source = "test")
    assertTrue(override is CheckInOutcome.Success)
    assertTrue(repo.getAdminActivityLogs().first().action.contains("Reason: Member ending pause early"))
  }

  @Test
  fun cancelled_booking_is_always_blocked() {
    // b6 Gautham Das: Cancelled
    assertEquals(CheckInEligibility.BLOCKED, repo.evaluateBookingCheckIn("b6").eligibility)
    assertTrue(repo.markBookingArrived("b6", reason = "Manager approved") is CheckInOutcome.Rejected)
  }

  @Test
  fun booking_already_checked_in_is_blocked() {
    // b3 Mohammed Faizal: Completed / Checked In
    assertEquals(CheckInEligibility.BLOCKED, repo.evaluateBookingCheckIn("b3").eligibility)
    assertTrue(repo.markBookingArrived("b3", reason = "Manager approved") is CheckInOutcome.Rejected)
  }

  @Test
  fun upcoming_booking_for_active_member_is_allowed() {
    // b1 Rahul Menon: Upcoming, member c1 Active
    assertEquals(CheckInEligibility.ALLOWED, repo.evaluateBookingCheckIn("b1").eligibility)
  }

  @Test
  fun day_pass_without_member_profile_checks_in_once() {
    // b7 Nikhil Thomas: Day Pass, no member record, Upcoming
    assertEquals(CheckInEligibility.ALLOWED, repo.evaluateBookingCheckIn("b7").eligibility)
    val scan = repo.processScannedQrPayload("BYCE-BOOKING-b7")
    assertEquals(ScannedQrType.BOOKING_PASS, scan.type)
    assertTrue(repo.confirmQrCheckIn(scan) is CheckInOutcome.Success)

    assertEquals(CheckInEligibility.BLOCKED, repo.evaluateBookingCheckIn("b7").eligibility)
    assertTrue("a used day pass must not check in again", repo.markBookingArrived("b7", reason = "Manager approved") is CheckInOutcome.Rejected)
  }

  @Test
  fun unknown_booking_code_is_blocked() {
    assertEquals(CheckInEligibility.BLOCKED, repo.evaluateBookingCheckIn("zz9").eligibility)
    assertTrue(repo.markBookingArrived("zz9", reason = "Manager approved") is CheckInOutcome.Rejected)
    val scan = repo.processScannedQrPayload("BYCE-BOOKING-zz9")
    assertTrue(repo.confirmQrCheckIn(scan, reason = "Manager approved") is CheckInOutcome.Rejected)
  }

  @Test
  fun unrecognized_membership_status_is_blocked_without_override() {
    repo.addCustomer(
      CustomerItem("t_status", "Test Unknown Status", "unknown.status@example.com", "+91 90000 00001",
        "Monthly", "01 Oct 2026", "31 Oct 2026", "Suspended", "01 Oct 2026", "TU")
    )
    assertEquals(CheckInEligibility.BLOCKED, repo.evaluateMemberCheckIn("t_status").eligibility)
    assertTrue(repo.checkIn(customerId = "t_status", reason = "Manager approved", source = "test") is CheckInOutcome.Rejected)
    assertTrue(repo.performAttendanceOverride("t_status", "QR Scanner Not Working") is CheckInOutcome.Rejected)
  }

  @Test
  fun hardware_override_still_requires_a_real_member_and_blocks_duplicates() {
    assertTrue(repo.performAttendanceOverride("not-a-member", "QR Scanner Not Working") is CheckInOutcome.Rejected)
    // c2 Ananya Nair is already checked in today
    assertTrue(repo.performAttendanceOverride("c2", "QR Scanner Not Working") is CheckInOutcome.Rejected)
  }

  @Test
  fun scanned_expired_member_needs_a_reason() {
    // c7 Pooja Krishnan: Expired
    val result = repo.processScannedQrPayload("BYCE-MEMBER-Pooja Krishnan-c7")
    assertEquals(CheckInEligibility.NEEDS_REASON, repo.evaluateScanCheckIn(result).eligibility)
    assertTrue(repo.confirmQrCheckIn(result) is CheckInOutcome.Rejected)
  }

  @Test
  fun manual_check_in_search_only_returns_existing_members() {
    assertTrue(repo.searchCustomers("Nobody Here").isEmpty())
    assertTrue(repo.searchCustomers("Sneha").any { it.id == "c5" })
    assertTrue(repo.checkIn(customerId = "made-up-id", source = "test") is CheckInOutcome.Rejected)
  }
}
