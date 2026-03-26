package com.addmrp.vault.domain.model

/**
 * Represents a reward rule for a specific spend category on a credit card.
 * Used by OptimalSwiperUseCase to calculate net savings.
 *
 * Rule 13 compliance: includes redemption fees, reward caps,
 * forex markups, and minimum spend requirements.
 */
data class CardRewardRule(
    /** Spend category this rule applies to */
    val category: SpendCategory,

    /** Direct cashback percentage (e.g., 5.0 for 5%) — 0 if points-based */
    val cashbackPercent: Double = 0.0,

    /** Reward points earned per ₹100 spent (e.g., 2.0) — 0 if cashback-based */
    val pointsPerHundredRupees: Double = 0.0,

    /** Monetary value of 1 reward point in ₹ (e.g., 0.25 means 4 pts = ₹1) */
    val pointValueInRupees: Double = 0.0,

    /** Maximum cashback/reward per month in ₹ (null = unlimited) */
    val monthlyCap: Double? = null,

    /** Cost per redemption in ₹ including GST (e.g., ₹99 + 18% GST = ₹116.82) */
    val redemptionFee: Double = 0.0,

    /** Minimum monthly spend in ₹ to unlock this reward tier (null = no minimum) */
    val minMonthlySpend: Double? = null,

    /** Forex markup percentage for international transactions (e.g., 3.5) */
    val forexMarkupPercent: Double = 0.0,

    /** Whether this rule is currently active (some rules are seasonal) */
    val isActive: Boolean = true
) {
    /**
     * Calculate net savings for a given spend amount.
     * Pure function — Rule 14 testability compliant.
     *
     * @param amount The transaction amount in ₹
     * @param isInternational Whether the transaction is international
     * @param currentMonthRedeemed How much cashback/reward already redeemed this month
     * @return Net savings in ₹ after deducting fees and caps
     */
    fun calculateNetSavings(
        amount: Double,
        isInternational: Boolean = false,
        currentMonthRedeemed: Double = 0.0
    ): Double {
        if (!isActive) return 0.0

        // Calculate gross reward
        val grossReward = if (cashbackPercent > 0) {
            amount * (cashbackPercent / 100.0)
        } else {
            val points = (amount / 100.0) * pointsPerHundredRupees
            points * pointValueInRupees
        }

        // Apply monthly cap (Rule 13: reward caps)
        val cappedReward = if (monthlyCap != null) {
            val remainingCap = (monthlyCap - currentMonthRedeemed).coerceAtLeast(0.0)
            grossReward.coerceAtMost(remainingCap)
        } else {
            grossReward
        }

        // Deduct redemption fee (Rule 13: redemption fees)
        val afterFees = (cappedReward - redemptionFee).coerceAtLeast(0.0)

        // Deduct forex markup if international (Rule 13: forex markups)
        val forexCost = if (isInternational) amount * (forexMarkupPercent / 100.0) else 0.0

        return (afterFees - forexCost).coerceAtLeast(0.0)
    }
}
