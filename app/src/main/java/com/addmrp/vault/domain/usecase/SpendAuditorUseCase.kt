package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.AdvisorInsight
import com.addmrp.vault.domain.model.CreditCard
import com.addmrp.vault.domain.model.InsightSeverity
import com.addmrp.vault.domain.model.InsightType
import com.addmrp.vault.domain.model.SpendCategory
import com.addmrp.vault.domain.model.Transaction
import javax.inject.Inject

/**
 * Monthly Spend Auditor — "Missed Savings" analyzer.
 *
 * PURE FUNCTION: (transactions, cards) → List<AdvisorInsight>
 *
 * Rule 12: Frames advice as "You could have saved" — never "You should spend more."
 * Rule 14: 100% testable — no Android dependencies.
 * Rule 16: If debt detected, disables reward tips.
 */
class SpendAuditorUseCase @Inject constructor() {

    /**
     * Analyze the past month's transactions and identify missed savings.
     * "You used a debit card for Swiggy — Axis Neo gives 40% off."
     */
    fun execute(
        transactions: List<Transaction>,
        cards: List<CreditCard>
    ): List<AdvisorInsight> {
        if (transactions.isEmpty()) return emptyList()
        if (cards.isEmpty()) return listOf(noCardsInsight())

        // Rule 16: Check for debt danger across all cards
        val debtCards = cards.filter { it.isInDebtDanger }
        if (debtCards.isNotEmpty()) {
            return listOf(debtModeInsight(debtCards))
        }

        val insights = mutableListOf<AdvisorInsight>()

        // 1. Find debit card transactions where a credit card would have saved money
        val debitTransactions = transactions.filter { it.isDebitCard }
        val missedSavingsByCategory = debitTransactions
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        missedSavingsByCategory.forEach { (category, totalSpend) ->
            val bestCard = findBestCardForCategory(category, cards, totalSpend)
            if (bestCard != null && bestCard.second > 0) {
                insights.add(
                    AdvisorInsight(
                        type = InsightType.MISSED_SAVINGS,
                        severity = InsightSeverity.WARNING,
                        headline = "Missed ₹${bestCard.second.toLong()} on ${category.displayName}",
                        description = "You spent ₹${totalSpend.toLong()} on ${category.displayName} " +
                                "using debit cards. If you had used your ${bestCard.first.cardName}, " +
                                "you would have saved ₹${bestCard.second.toLong()}.",
                        savingsAmount = bestCard.second,
                        recommendedCardId = bestCard.first.id,
                        recommendedCardName = bestCard.first.cardName,
                        actionLabel = "Set Reminder"
                    )
                )
            }
        }

        // 2. Find credit card transactions where a different card would have been better
        val creditTransactions = transactions.filter { !it.isDebitCard }
        val txnsByCardAndCategory = creditTransactions
            .groupBy { Pair(it.cardId, it.category) }

        txnsByCardAndCategory.forEach { (key, txns) ->
            val (usedCardId, category) = key
            val totalSpend = txns.sumOf { it.amount }
            val usedCard = cards.find { it.id == usedCardId } ?: return@forEach

            val usedRule = usedCard.bestRuleForCategory(category)
            val usedSavings = usedRule?.calculateNetSavings(totalSpend) ?: 0.0

            val bestCard = findBestCardForCategory(category, cards, totalSpend)
            if (bestCard != null && bestCard.first.id != usedCardId && bestCard.second > usedSavings + 50) {
                val missedAmount = bestCard.second - usedSavings
                insights.add(
                    AdvisorInsight(
                        type = InsightType.MISSED_SAVINGS,
                        severity = InsightSeverity.INFO,
                        headline = "Could have saved ₹${missedAmount.toLong()} more on ${category.displayName}",
                        description = "You used ${usedCard.cardName} for ₹${totalSpend.toLong()} on " +
                                "${category.displayName} (saved ₹${usedSavings.toLong()}). " +
                                "${bestCard.first.cardName} would have saved ₹${bestCard.second.toLong()} — " +
                                "that's ₹${missedAmount.toLong()} more.",
                        savingsAmount = missedAmount,
                        recommendedCardId = bestCard.first.id,
                        recommendedCardName = bestCard.first.cardName,
                        alternativeCardName = usedCard.cardName,
                        alternativeReason = "Only provided ₹${usedSavings.toLong()} savings."
                    )
                )
            }
        }

        // Sort by savings amount (highest missed savings first)
        return insights.sortedByDescending { it.savingsAmount }
    }

    /**
     * Calculate total potential savings for a period.
     * Pure function — suitable for the Savings Dial widget.
     */
    fun calculateTotalMissedSavings(
        transactions: List<Transaction>,
        cards: List<CreditCard>
    ): Double {
        return execute(transactions, cards).sumOf { it.savingsAmount }
    }

    private fun findBestCardForCategory(
        category: SpendCategory,
        cards: List<CreditCard>,
        amount: Double
    ): Pair<CreditCard, Double>? {
        return cards.map { card ->
            val rule = card.bestRuleForCategory(category)
            val savings = rule?.calculateNetSavings(amount) ?: (amount * card.defaultCashbackPercent / 100.0)
            card to savings
        }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }
    }

    private fun noCardsInsight() = AdvisorInsight(
        type = InsightType.MISSED_SAVINGS,
        severity = InsightSeverity.INFO,
        headline = "Add your cards to see savings",
        description = "Once you add your credit cards, we'll analyze your spending " +
                "and show you where you could have saved money."
    )

    private fun debtModeInsight(debtCards: List<CreditCard>): AdvisorInsight {
        val cardNames = debtCards.joinToString(", ") { it.cardName }
        return AdvisorInsight(
            type = InsightType.DEBT_WARNING,
            severity = InsightSeverity.CRITICAL,
            headline = "Focus on debt payoff first",
            description = "Your card(s) ($cardNames) show revolving credit. " +
                    "At ~42% APR, interest charges far exceed any potential rewards. " +
                    "Clear your outstanding balance before optimizing for savings.",
            isDebtModeInsight = true
        )
    }
}
