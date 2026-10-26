package com.example.data.services

/**
 * Phone + OTP login, kept behind an interface so a real backend (SMS provider, auth API)
 * can replace [DemoPhoneAuthService] without touching the login UI.
 */
interface PhoneAuthService {
    /** True when this implementation is a local demo that never sends or checks a real SMS. */
    val isDemo: Boolean

    /** Asks the backend to send a one-time code to [phone]. */
    suspend fun requestOtp(phone: PhoneNumber): OtpRequestResult

    /** Checks [code] for [phone]. */
    suspend fun verifyOtp(phone: PhoneNumber, code: String): OtpVerifyResult
}

sealed interface OtpRequestResult {
    data object CodeRequested : OtpRequestResult
    data class Failed(val message: String) : OtpRequestResult
}

sealed interface OtpVerifyResult {
    data object Verified : OtpVerifyResult
    data class Rejected(val message: String) : OtpVerifyResult
}

data class CountryCode(val dialCode: String, val isoCode: String, val nationalLength: Int)

data class PhoneNumber(val country: CountryCode, val nationalNumber: String) {
    val display: String get() = "${country.dialCode} $nationalNumber"
}

object PhoneValidation {
    const val OTP_LENGTH = 6

    val countryCodes = listOf(
        CountryCode(dialCode = "+91", isoCode = "IN", nationalLength = 10),
        CountryCode(dialCode = "+971", isoCode = "AE", nationalLength = 9),
        CountryCode(dialCode = "+1", isoCode = "US", nationalLength = 10),
        CountryCode(dialCode = "+44", isoCode = "GB", nationalLength = 10)
    )

    val defaultCountry: CountryCode get() = countryCodes.first()

    /** Keeps only digits, capped at the country's national number length. */
    fun sanitize(input: String, country: CountryCode): String =
        input.filter { it.isDigit() }.take(country.nationalLength)

    /** Returns an error message, or null when [nationalNumber] is valid for [country]. */
    fun validate(nationalNumber: String, country: CountryCode): String? = when {
        nationalNumber.isEmpty() -> "Enter your phone number."
        nationalNumber.any { !it.isDigit() } -> "Use digits only."
        nationalNumber.length != country.nationalLength ->
            "Enter a ${country.nationalLength}-digit phone number."
        // Indian mobile numbers start with 6, 7, 8 or 9
        country.isoCode == "IN" && nationalNumber.first() !in '6'..'9' -> "Enter a valid mobile number."
        else -> null
    }

    fun isValidOtp(code: String): Boolean = code.length == OTP_LENGTH && code.all { it.isDigit() }
}

/**
 * Local demo only: no SMS is sent and nothing is checked by a server.
 * Any valid phone number is accepted and [DEMO_CODE] is the only code that verifies.
 */
class DemoPhoneAuthService : PhoneAuthService {
    override val isDemo: Boolean = true

    override suspend fun requestOtp(phone: PhoneNumber): OtpRequestResult {
        val error = PhoneValidation.validate(phone.nationalNumber, phone.country)
        return if (error == null) OtpRequestResult.CodeRequested else OtpRequestResult.Failed(error)
    }

    override suspend fun verifyOtp(phone: PhoneNumber, code: String): OtpVerifyResult = when {
        !PhoneValidation.isValidOtp(code) -> OtpVerifyResult.Rejected("Enter the 6-digit code.")
        code != DEMO_CODE -> OtpVerifyResult.Rejected("That code isn't correct. Check it and try again.")
        else -> OtpVerifyResult.Verified
    }

    companion object {
        const val DEMO_CODE = "123456"
    }
}
