package com.addmrp.vault.domain.model

/**
 * Type of AI advisory insight.
 */
enum class InsightType(val displayName: String) {
    OPTIMAL_CARD("Best Card Suggestion"),
    MISSED_SAVINGS("Missed Savings"),
    POINT_EXPIRY("Points Expiring"),
    DEBT_WARNING("Debt Alert"),
    REWARD_CAP("Reward Cap Reached"),
    FEE_WARNING("Hidden Fee Alert"),
    HABIT_TIP("Savings Habit Tip")
}

/**
 * Severity of an insight — affects UI treatment (color, banner type).
 */
enum class InsightSeverity {
    INFO,       // Blue (VaultPrimary)
    SUCCESS,    // Green (VaultGreen) — savings achieved
    WARNING,    // Orange (VaultOrange) — approaching cap/expiry
    CRITICAL    // Red (VaultRed) — debt warning, expired points
}

/**
 * AI Advisor recommendation output.
 *
 * Rule 12: Every insight passes through FrugalityFilter before reaching UI.
 * Rule 13: Savings calculation includes hidden costs.
 * Rule 16: If debtModeActive, type should be DEBT_WARNING.
 *
 * This is a VIEW-ready model — it contains the final formatted strings
 * for direct binding to UI composables.
 */
data class AdvisorInsight(
    /** Type of this insight */
    val type: InsightType,

    /** Severity level — determines UI color treatment */
    val severity: InsightSeverity = InsightSeverity.INFO,

    /** Headline — e.g., "Use SBI Cashback for 5% back" */
    val headline: String,

    /** Detailed explanation — e.g., "Saves ₹250 on this ₹5,000 purchase..." */
    val description: String,

    /** Net savings in ₹ (positive = savings, negative = loss from fees) */
    val savingsAmount: Double = 0.0,

    /** Recommended card ID (null for non-card-specific insights) */
    val recommendedCardId: String? = null,

    /** Recommended card name for display */
    val recommendedCardName: String? = null,

    /** Alternative card suggestion (if any) */
    val alternativeCardName: String? = null,

    /** Reason to avoid the alternative */
    val alternativeReason: String? = null,

    /** Action label for CTA button (null = no action) */
    val actionLabel: String? = null,

    /** Whether this insight has been dismissed by user */
    val isDismissed: Boolean = false,

    /** Whether this insight was generated in debt mode (Rule 16) */
    val isDebtModeInsight: Boolean = false
) {
    /**
     * Formatted savings string for UI.
     * Positive = "₹250 saved", Negative = "₹50 in fees"
     */
    val formattedSavings: String
        get() = when {
            savingsAmount > 0 -> "₹${savingsAmount.toLong()} saved"
            savingsAmount < 0 -> "₹${(-savingsAmount).toLong()} in fees"
            else -> ""
        }
}
