package com.addmrp.vault.domain.model

/**
 * Source platform from which the voucher was obtained.
 */
enum class RedemptionSource(val displayName: String) {
    GPAY("GPay"),
    PHONEPE("PhonePe"),
    CRED("CRED"),
    MANUAL("Manual"),
    SMS("SMS Scraped"),
    EMAIL("Email Scraped");

    companion object {
        fun fromString(value: String): RedemptionSource =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: MANUAL
    }
}
