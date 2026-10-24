package com.example

import com.example.data.models.ScannedQrType
import com.example.data.services.GymOwnerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ScannerPayloadTest {

  private fun assertCannotCheckIn(payload: String) {
    val result = GymOwnerRepository.processScannedQrPayload(payload)
    assertFalse("'$payload' should not be recognized", result.isRecognizedGymEntity)
    assertEquals(ScannedQrType.UNIVERSAL_CODE, result.type)
    assertNull("'$payload' should not check anyone in", GymOwnerRepository.confirmQrCheckIn(result))
  }

  @Test
  fun unknown_member_pass_cannot_check_in() {
    assertCannotCheckIn("BYCE-MEMBER-Unknown Person-MEM-0000")
  }

  @Test
  fun unknown_booking_pass_cannot_check_in() {
    assertCannotCheckIn("BYCE-BOOKING-zz9-Nobody")
  }

  @Test
  fun id_inside_another_word_does_not_match() {
    // Contains "c1" and "b1" as substrings, but not as whole tokens
    assertCannotCheckIn("https://abc1.example.com/sub1")
  }

  @Test
  fun upi_code_is_not_treated_as_verified_payment() {
    assertCannotCheckIn("upi://pay?pa=someone@upi&pn=Someone&am=1499.00&cu=INR")
  }

  @Test
  fun known_member_pass_is_recognized() {
    val result = GymOwnerRepository.processScannedQrPayload("BYCE-MEMBER-Rahul Menon-c1")
    assertEquals(ScannedQrType.MEMBER_CHECKIN, result.type)
    assertEquals("Rahul Menon", result.customerName)
  }

  @Test
  fun known_booking_pass_is_recognized() {
    val result = GymOwnerRepository.processScannedQrPayload("BYCE-BOOKING-b2-Ananya Nair")
    assertEquals(ScannedQrType.BOOKING_PASS, result.type)
    assertEquals("Ananya Nair", result.customerName)
  }
}
