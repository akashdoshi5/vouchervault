package com.addmrp.vault.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Reward point valuation tier — represents one redemption option.
 * e.g., "10,000 pts = ₹10,000 via Tanishq Vouchers" vs. "₹5,000 via Statement Credit"
 */
data class ValuationTier(
    /** Redemption channel name */
    val channelName: String,

    /** Value per point in ₹ for this channel */
    val valuePerPoint: Double,

    /** Total ₹ value for given points */
    val totalValue: Double,

    /** Redemption fee for this channel */
    val fee: Double = 0.0,

    /** Net value after fees */
    val netValue: Double = 0.0,

    /** Whether this is the best-value option */
    val isBestValue: Boolean = false
)

/**
 * Domain model for reward point balance per credit card.
 *
 * Rule 4: Expiry dates in UTC.
 * Rule 13: Includes valuation with fee deductions.
 */
data class RewardPoint(
    val id: String = UUID.randomUUID().toString(),

    /** Credit card ID this balance belongs to */
    val cardId: String,

    /** Card name (denormalized for display) */
    val cardName: String = "",

    /** Card issuer */
    val issuer: CardIssuer = CardIssuer.OTHER,

    /** Current points balance */
    val balance: Int = 0,

    /** Available redemption tiers with valuations */
    val valuationTiers: List<ValuationTier> = emptyList(),

    /** Best monetary value in ₹ across all tiers */
    val bestValueInRupees: Double = 0.0,

    /** Points expiry date in UTC (null = no expiry) — Rule 4 */
    val expiryUtc: Instant? = null,

    /** When this balance was last updated — Rule 4 */
    val lastUpdatedUtc: Instant = Instant.now(),

    /** Firestore user ID for cloud sync */
    val userId: String = ""
) {
    /** Whether points are expiring within 30 days */
    val isExpiringSoon: Boolean
        get() = expiryUtc != null &&
                !Instant.now().isAfter(expiryUtc) &&
                java.time.Duration.between(Instant.now(), expiryUtc).toDays() <= 30

    /** Whether points have already expired */
    val isExpired: Boolean
        get() = expiryUtc != null && Instant.now().isAfter(expiryUtc)

    /** Days until expiry (negative = already expired) */
    val daysUntilExpiry: Long?
        get() = expiryUtc?.let {
            java.time.Duration.between(Instant.now(), it).toDays()
        }
}
