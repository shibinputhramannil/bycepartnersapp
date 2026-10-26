package com.example

import com.example.data.services.DemoPhoneAuthService
import com.example.data.services.OtpRequestResult
import com.example.data.services.OtpVerifyResult
import com.example.data.services.PhoneNumber
import com.example.data.services.PhoneValidation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneAuthTest {

  private val india = PhoneValidation.countryCodes.first { it.isoCode == "IN" }
  private val uae = PhoneValidation.countryCodes.first { it.isoCode == "AE" }
  private val service = DemoPhoneAuthService()

  @Test
  fun valid_indian_mobile_number_passes() {
    assertNull(PhoneValidation.validate("9847012345", india))
    assertNull(PhoneValidation.validate("6000000000", india))
  }

  @Test
  fun empty_short_long_and_non_digit_numbers_fail() {
    assertEquals("Enter your phone number.", PhoneValidation.validate("", india))
    assertEquals("Enter a 10-digit phone number.", PhoneValidation.validate("98470", india))
    assertEquals("Enter a 10-digit phone number.", PhoneValidation.validate("98470123456", india))
    assertEquals("Use digits only.", PhoneValidation.validate("98470-1234", india))
  }

  @Test
  fun indian_numbers_must_start_with_6_to_9() {
    assertEquals("Enter a valid mobile number.", PhoneValidation.validate("1234567890", india))
    assertEquals("Enter a valid mobile number.", PhoneValidation.validate("5847012345", india))
  }

  @Test
  fun length_follows_the_selected_country() {
    assertNull(PhoneValidation.validate("501234567", uae))
    assertNotNull(PhoneValidation.validate("501234567", india))
  }

  @Test
  fun sanitize_strips_formatting_and_caps_length() {
    assertEquals("9847012345", PhoneValidation.sanitize("98470 12345", india))
    assertEquals("9847012345", PhoneValidation.sanitize("(984) 701-2345 99", india))
    assertEquals("501234567", PhoneValidation.sanitize("50 123 4567 8", uae))
  }

  @Test
  fun otp_must_be_six_digits() {
    assertTrue(PhoneValidation.isValidOtp("123456"))
    assertEquals(false, PhoneValidation.isValidOtp("12345"))
    assertEquals(false, PhoneValidation.isValidOtp("1234567"))
    assertEquals(false, PhoneValidation.isValidOtp("12a456"))
  }

  @Test
  fun demo_service_is_labelled_as_demo() {
    assertTrue(service.isDemo)
  }

  @Test
  fun demo_request_accepts_valid_and_rejects_invalid_numbers() = runTest {
    assertEquals(OtpRequestResult.CodeRequested, service.requestOtp(PhoneNumber(india, "9847012345")))
    assertTrue(service.requestOtp(PhoneNumber(india, "12345")) is OtpRequestResult.Failed)
  }

  @Test
  fun demo_verification_accepts_only_the_demo_code() = runTest {
    val phone = PhoneNumber(india, "9847012345")
    assertEquals(OtpVerifyResult.Verified, service.verifyOtp(phone, DemoPhoneAuthService.DEMO_CODE))
    assertEquals(
      OtpVerifyResult.Rejected("That code isn't correct. Check it and try again."),
      service.verifyOtp(phone, "654321")
    )
    assertEquals(OtpVerifyResult.Rejected("Enter the 6-digit code."), service.verifyOtp(phone, "123"))
  }
}
