package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.CreditCard
import javax.inject.Inject

/**
 * Debt detection engine.
 *
 * PURE FUNCTION: (cards) → DebtStatus
 * Rule 16: Detects revolving credit and high utilization.
 *          When active, all reward tips are replaced with debt payoff tips.
 * Rule 14: 100% testable — no Android dependencies.
 */
class DebtDetectorUseCase @Inject constructor() {

    /**
     * Analyze all cards and determine if user is in debt danger.
     */
    fun execute(cards: List<CreditCard>): DebtStatus {
        if (cards.isEmpty()) return DebtStatus.HEALTHY

        val revolvingCards = cards.filter { it.isRevolvingCredit }
        val highUtilCards = cards.filter { it.utilizationPercent > 70.0 }
        val dangerCards = (revolvingCards + highUtilCards).distinctBy { it.id }

        return when {
            revolvingCards.isNotEmpty() -> DebtStatus.CRITICAL(
                affectedCards = dangerCards,
                totalOutstanding = dangerCards.sumOf { it.currentBalance ?: 0.0 },
                estimatedMonthlyInterest = calculateInterest(dangerCards),
                recommendation = buildCriticalRecommendation(dangerCards)
            )
            highUtilCards.isNotEmpty() -> DebtStatus.WARNING(
                affectedCards = highUtilCards,
                avgUtilization = highUtilCards.map { it.utilizationPercent }.average(),
                recommendation = buildWarningRecommendation(highUtilCards)
            )
            else -> DebtStatus.HEALTHY
        }
    }

    /**
     * Quick check if debt mode should be active.
     * Used by FrugalityFilter and UI components.
     */
    fun isDebtModeActive(cards: List<CreditCard>): Boolean {
        return cards.any { it.isInDebtDanger }
    }

    private fun calculateInterest(cards: List<CreditCard>): Double {
        // Indian credit cards typically charge 3.5% per month (~42% APR)
        val monthlyRate = 0.035
        return cards.sumOf { (it.currentBalance ?: 0.0) * monthlyRate }
    }

    private fun buildCriticalRecommendation(cards: List<CreditCard>): String {
        val cardNames = cards.joinToString(", ") { it.cardName }
        val totalBalance = cards.sumOf { it.currentBalance ?: 0.0 }
        val monthlyInterest = calculateInterest(cards)
        return "URGENT: Clear ₹${totalBalance.toLong()} outstanding on $cardNames. " +
                "You're paying ~₹${monthlyInterest.toLong()}/month in interest charges alone. " +
                "That's ₹${(monthlyInterest * 12).toLong()}/year wasted — far more than any reward points."
    }

    private fun buildWarningRecommendation(cards: List<CreditCard>): String {
        val cardNames = cards.joinToString(", ") { it.cardName }
        return "Your credit utilization on $cardNames is above 70%. " +
                "High utilization hurts your credit score and may trigger higher interest rates. " +
                "Try to keep utilization below 30% for optimal credit health."
    }
}

/**
 * Sealed class for debt analysis results.
 * Used by UI to decide whether to show reward tips or debt warnings.
 */
sealed class DebtStatus {
    /** No debt issues detected — show normal reward optimization */
    data object HEALTHY : DebtStatus()

    /** High utilization detected — show utilization warning but allow reward tips */
    data class WARNING(
        val affectedCards: List<CreditCard>,
        val avgUtilization: Double,
        val recommendation: String
    ) : DebtStatus()

    /** Revolving credit detected — disable reward tips, show debt payoff mode */
    data class CRITICAL(
        val affectedCards: List<CreditCard>,
        val totalOutstanding: Double,
        val estimatedMonthlyInterest: Double,
        val recommendation: String
    ) : DebtStatus()
}
