package com.addmrp.vault.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Indian credit card issuers supported by VoucherVault.
 */
enum class CardIssuer(val displayName: String, val shortCode: String) {
    HDFC("HDFC Bank", "HDFC"),
    SBI("SBI Card", "SBI"),
    ICICI("ICICI Bank", "ICICI"),
    AXIS("Axis Bank", "AXIS"),
    KOTAK("Kotak Mahindra", "KOTAK"),
    RBL("RBL Bank", "RBL"),
    AMEX("American Express", "AMEX"),
    CITI("Citibank", "CITI"),
    IDFC("IDFC First", "IDFC"),
    YES("YES Bank", "YES"),
    INDUSIND("IndusInd Bank", "INDUS"),
    AU("AU Small Finance", "AU"),
    FEDERAL("Federal Bank", "FED"),
    BOB("Bank of Baroda", "BOB"),
    OTHER("Other", "OTHER")
}

/**
 * Credit card reward type.
 */
enum class RewardType(val displayName: String) {
    CASHBACK("Cashback"),
    POINTS("Reward Points"),
    MILES("Air Miles"),
    MIXED("Mixed (Cashback + Points)")
}

/**
 * Domain model for a user's Credit Card.
 *
 * Privacy Rule 15: Card numbers are NEVER stored.
 * Only the last 4 digits and card name are persisted.
 * All financial data is encrypted at rest in Room DB.
 *
 * Rule 4: All timestamps in UTC epoch millis.
 */
data class CreditCard(
    val id: String = UUID.randomUUID().toString(),

    /** User-facing card name (e.g., "HDFC Regalia", "SBI Cashback") */
    val cardName: String,

    /** Card issuer bank */
    val issuer: CardIssuer,

    /** Last 4 digits ONLY — Rule 15: never store full card number */
    val lastFourDigits: String = "",

    /** Reward type for this card */
    val rewardType: RewardType = RewardType.CASHBACK,

    /** Category-specific reward rules */
    val rewardRules: List<CardRewardRule> = emptyList(),

    /** Default cashback % for categories without specific rules */
    val defaultCashbackPercent: Double = 0.0,

    /** Annual fee in ₹ (0 for lifetime-free cards) */
    val annualFee: Double = 0.0,

    /** Whether annual fee is waived on meeting spend target */
    val annualFeeWaiverSpend: Double? = null,

    /** Credit limit in ₹ (for utilization tracking — Rule 16) */
    val creditLimit: Double? = null,

    /** Current outstanding balance in ₹ (for debt detection — Rule 16) */
    val currentBalance: Double? = null,

    /** Whether user is only paying minimum due (revolving credit flag) */
    val isRevolvingCredit: Boolean = false,

    /** Total reward points balance */
    val rewardPointsBalance: Int = 0,

    /** Reward points expiry date in UTC (null = no expiry) */
    val pointsExpiryUtc: Instant? = null,

    /** User's total spend this month on this card */
    val currentMonthSpend: Double = 0.0,

    /** User's total cashback/rewards redeemed this month */
    val currentMonthRedeemed: Double = 0.0,

    /** Card added timestamp — Rule 4: UTC */
    val createdAtUtc: Instant = Instant.now(),

    /** Last updated timestamp — Rule 4: UTC */
    val updatedAtUtc: Instant = Instant.now(),

    /** Firestore user ID for cloud sync */
    val userId: String = ""
) {
    /** Credit utilization percentage (Rule 16: >70% triggers debt warning) */
    val utilizationPercent: Double
        get() = if (creditLimit != null && creditLimit > 0 && currentBalance != null) {
            (currentBalance / creditLimit) * 100.0
        } else 0.0

    /** Whether this card is in a debt-danger state (Rule 16) */
    val isInDebtDanger: Boolean
        get() = isRevolvingCredit || utilizationPercent > 70.0

    /** Whether reward points are expiring within 30 days */
    val isPointsExpiringSoon: Boolean
        get() = pointsExpiryUtc != null &&
                !Instant.now().isAfter(pointsExpiryUtc) &&
                java.time.Duration.between(Instant.now(), pointsExpiryUtc).toDays() <= 30

    /** Get the best reward rule for a given spend category */
    fun bestRuleForCategory(category: SpendCategory): CardRewardRule? {
        return rewardRules
            .filter { it.category == category && it.isActive }
            .maxByOrNull { it.cashbackPercent + (it.pointsPerHundredRupees * it.pointValueInRupees) }
    }
}
