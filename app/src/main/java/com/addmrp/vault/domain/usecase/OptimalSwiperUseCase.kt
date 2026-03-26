package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.AdvisorInsight
import com.addmrp.vault.domain.model.CardRewardRule
import com.addmrp.vault.domain.model.CreditCard
import com.addmrp.vault.domain.model.InsightSeverity
import com.addmrp.vault.domain.model.InsightType
import com.addmrp.vault.domain.model.SpendCategory
import javax.inject.Inject

/**
 * The "Which Card?" engine.
 *
 * PURE FUNCTION: (query, cards) → AdvisorInsight
 * Rule 12: Never suggests a new purchase — only optimizes existing/planned spend.
 * Rule 13: Factors in redemption fees, caps, forex markups.
 * Rule 14: 100% testable — no Android dependencies.
 * Rule 16: If any card is in debt danger, insight is overridden.
 */
class OptimalSwiperUseCase @Inject constructor() {

    /**
     * Given a user query (e.g., "Buying Groceries at Blinkit") and their cards,
     * returns the optimal card recommendation with savings explanation.
     */
    fun execute(query: String, cards: List<CreditCard>, amount: Double = 1000.0): AdvisorInsight {
        if (cards.isEmpty()) {
            return AdvisorInsight(
                type = InsightType.OPTIMAL_CARD,
                severity = InsightSeverity.INFO,
                headline = "Add your credit cards first",
                description = "Add your credit cards to get personalized recommendations."
            )
        }

        // Rule 16: Check if any card is in debt danger
        val debtCards = cards.filter { it.isInDebtDanger }
        if (debtCards.isNotEmpty()) {
            return createDebtWarning(debtCards.first())
        }

        // Detect spend category from query
        val category = SpendCategory.fromMerchant(query)

        // Rank all cards by net savings for this category
        val rankings = cards.map { card ->
            val rule = card.bestRuleForCategory(category)
            val savings = rule?.calculateNetSavings(
                amount = amount,
                isInternational = category == SpendCategory.INTERNATIONAL,
                currentMonthRedeemed = card.currentMonthRedeemed
            ) ?: (amount * card.defaultCashbackPercent / 100.0)

            CardRanking(card, rule, savings)
        }.sortedByDescending { it.netSavings }

        val best = rankings.first()
        val alternative = rankings.getOrNull(1)

        return if (best.netSavings <= 0) {
            AdvisorInsight(
                type = InsightType.OPTIMAL_CARD,
                severity = InsightSeverity.INFO,
                headline = "No significant savings available",
                description = "None of your cards offer meaningful rewards for ${category.displayName}. Consider paying with the card that has the lowest interest rate.",
                savingsAmount = 0.0
            )
        } else {
            AdvisorInsight(
                type = InsightType.OPTIMAL_CARD,
                severity = InsightSeverity.SUCCESS,
                headline = "Use ${best.card.cardName} for ${formatPercent(best)}",
                description = buildRecommendationDescription(best, alternative, category, amount),
                savingsAmount = best.netSavings,
                recommendedCardId = best.card.id,
                recommendedCardName = best.card.cardName,
                alternativeCardName = alternative?.card?.cardName,
                alternativeReason = alternative?.let {
                    "Only gives ${formatPercent(it)} on ${category.displayName}."
                }
            )
        }
    }

    private fun createDebtWarning(card: CreditCard): AdvisorInsight = AdvisorInsight(
        type = InsightType.DEBT_WARNING,
        severity = InsightSeverity.CRITICAL,
        headline = "Pay off ${card.cardName} first",
        description = "Your ${card.cardName} shows revolving credit at ~42% APR interest. " +
                "Reward points are irrelevant when you're paying this much in interest charges. " +
                "Focus on clearing the outstanding balance before optimizing for rewards.",
        isDebtModeInsight = true,
        recommendedCardName = card.cardName
    )

    private fun formatPercent(ranking: CardRanking): String {
        val rule = ranking.rule
        return if (rule != null && rule.cashbackPercent > 0) {
            "${rule.cashbackPercent}% cashback"
        } else if (rule != null && rule.pointsPerHundredRupees > 0) {
            "${rule.pointsPerHundredRupees} pts/₹100"
        } else {
            "${ranking.card.defaultCashbackPercent}% default"
        }
    }

    private fun buildRecommendationDescription(
        best: CardRanking,
        alternative: CardRanking?,
        category: SpendCategory,
        amount: Double
    ): String {
        val sb = StringBuilder()
        sb.append("₹${best.netSavings.toLong()} savings on this ₹${amount.toLong()} ${category.displayName} purchase. ")

        // Rule 13: Surface hidden costs
        best.rule?.let { rule ->
            if (rule.monthlyCap != null) {
                val remaining = (rule.monthlyCap - best.card.currentMonthRedeemed).coerceAtLeast(0.0)
                sb.append("Monthly cap: ₹${rule.monthlyCap.toLong()} (₹${remaining.toLong()} remaining). ")
            }
            if (rule.redemptionFee > 0) {
                sb.append("Redemption fee of ₹${rule.redemptionFee.toLong()} already deducted. ")
            }
        }

        alternative?.let {
            sb.append("Avoid ${it.card.cardName} — it only gives ${formatPercent(it)} on this category.")
        }

        return sb.toString().trim()
    }

    private data class CardRanking(
        val card: CreditCard,
        val rule: CardRewardRule?,
        val netSavings: Double
    )
}
