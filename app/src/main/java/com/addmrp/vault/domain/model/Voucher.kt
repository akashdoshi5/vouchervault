package com.addmrp.vault.domain.model

import java.time.Duration
import java.time.Instant

/**
 * Core domain model for a Voucher.
 * All timestamps are stored in UTC (Rule 4: Timezone & Expiry Rigidity).
 */
data class Voucher(
    val id: String = "",
    val brand: String = "",
    val category: VoucherCategory = VoucherCategory.OTHER,
    val code: String = "",
    val value: Double = 0.0,
    val valueLabel: String = "", // e.g. "₹150 OFF", "BOGO FREE", "20% OFF"
    val source: RedemptionSource = RedemptionSource.MANUAL,
    val expiryUtc: Instant = Instant.now(),
    val createdAtUtc: Instant = Instant.now(),
    val updatedAtUtc: Instant = Instant.now(),
    val isRedeemed: Boolean = false,
    val ownerId: String = "",
    val sharedWith: List<String> = emptyList(),
    val addedBy: String = "", // Display name of person who added
    val brandLogoUrl: String = "",
    val notes: String = "",
    // Real-Time Lock & Conflict Resolution (Rule 22)
    val inUseByUserId: String? = null,
    val inUseTimestampMillis: Long? = null,
    val lastUpdatedBy: String? = null
) {
    /** Time remaining until expiry. Returns Duration.ZERO if already expired. */
    fun timeUntilExpiry(): Duration {
        val remaining = Duration.between(Instant.now(), expiryUtc)
        return if (remaining.isNegative) Duration.ZERO else remaining
    }

    /** True if expiry is less than 24 hours away. */
    val isExpiringSoon: Boolean
        get() = !isExpired && timeUntilExpiry() < Duration.ofHours(24)

    /** True if the voucher has passed its expiry date. */
    val isExpired: Boolean
        get() = Instant.now().isAfter(expiryUtc)

    /** Formatted expiry countdown string for the UI. */
    fun expiryCountdownText(): String {
        if (isExpired) return "EXPIRED"
        val dur = timeUntilExpiry()
        val days = dur.toDays()
        val hours = dur.toHours() % 24
        val minutes = dur.toMinutes() % 60
        return when {
            days > 0 -> "Expires in ${days}D"
            hours > 0 -> "Expiring in ${hours}H"
            else -> "Expiring in ${minutes}M"
        }
    }
}
