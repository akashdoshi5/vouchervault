package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.*
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

/**
 * Reward Intelligence Engine.
 *
 * Pure function (Rule 14) that provides:
 * 1. Monthly reward summary (earned/redeemed/pending per card)
 * 2. Unused reward alerts (points about to expire)
 * 3. Card benefit summary
 * 4. AI card advisor (suggest better card for spend pattern)
 *
 * Rules: 12 (frugality), 13 (hidden costs), 15 (local-only), 16 (debt override)
 */
class RewardIntelligenceUseCase {

    data class MonthlyCardSummary(
        val cardName: String,
        val cardId: String,
        val issuer: CardIssuer,
        val totalSpend: Double,
        val pointsEarned: Int,
        val pointsRedeemed: Int,
        val pointsPending: Int,
        val pendingValueInRupees: Double,
        val topCategory: SpendCategory?,
        val topCategorySpend: Double
    )

    data class ExpiringReward(
        val cardName: String,
        val cardId: String,
        val pointBalance: Int,
        val valueInRupees: Double,
        val daysUntilExpiry: Long,
        val expiryLabel: String,
        val isUrgent: Boolean // < 7 days
    )

    data class CardBenefit(
        val category: String,
        val cashbackPercent: Double,
        val monthlyCap: String?,
        val note: String?
    )

    data class CardSuggestion(
        val suggestedCardName: String,
        val reason: String,
        val potentialExtraSavings: Double,
        val forCategory: SpendCategory
    )

    // ═══════════════════════════════════════════
    //  1. Monthly Reward Summary
    // ═══════════════════════════════════════════

    fun getMonthlyRewardSummary(
        cards: List<CreditCard>,
        transactions: List<Transaction>
    ): List<MonthlyCardSummary> {
        val now = Instant.now()
        val currentMonth = YearMonth.now(ZoneId.systemDefault())
        val monthStart = currentMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        return cards.map { card ->
            val cardTxns = transactions.filter {
                it.cardId == card.id && it.transactionDateUtc.isAfter(monthStart)
            }
            val totalSpend = cardTxns.sumOf { it.amount }

            // Calculate points earned this month based on card rules
            val pointsEarned = calculatePointsEarned(card, cardTxns)
            val pointsRedeemed = card.currentMonthRedeemed.toInt()
            val pointsPending = card.rewardPointsBalance

            // Find top spend category
            val categorySpends = cardTxns.groupBy { it.category }
                .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            val topEntry = categorySpends.maxByOrNull { it.value }

            val pointValue = calculatePointValue(card.issuer, pendingPoints = pointsPending)

            MonthlyCardSummary(
                cardName = card.cardName,
                cardId = card.id,
                issuer = card.issuer,
                totalSpend = totalSpend,
                pointsEarned = pointsEarned,
                pointsRedeemed = pointsRedeemed,
                pointsPending = pointsPending,
                pendingValueInRupees = pointValue,
                topCategory = topEntry?.key,
                topCategorySpend = topEntry?.value ?: 0.0
            )
        }
    }

    // ═══════════════════════════════════════════
    //  2. Unused / Expiring Rewards
    // ═══════════════════════════════════════════

    fun getExpiringRewards(cards: List<CreditCard>): List<ExpiringReward> {
        val now = Instant.now()
        return cards
            .filter { it.rewardPointsBalance > 0 && it.pointsExpiryUtc != null }
            .map { card ->
                val expiry = card.pointsExpiryUtc!!
                val daysUntil = Duration.between(now, expiry).toDays()
                val value = calculatePointValue(card.issuer, card.rewardPointsBalance)

                ExpiringReward(
                    cardName = card.cardName,
                    cardId = card.id,
                    pointBalance = card.rewardPointsBalance,
                    valueInRupees = value,
                    daysUntilExpiry = daysUntil,
                    expiryLabel = when {
                        daysUntil <= 0 -> "EXPIRED"
                        daysUntil <= 7 -> "⚠️ ${daysUntil}D left"
                        daysUntil <= 30 -> "${daysUntil}D left"
                        else -> "${daysUntil / 30}M left"
                    },
                    isUrgent = daysUntil in 0..7
                )
            }
            .filter { it.daysUntilExpiry > 0 } // Exclude already expired
            .sortedBy { it.daysUntilExpiry }
    }

    // ═══════════════════════════════════════════
    //  3. Card Benefit Summary
    // ═══════════════════════════════════════════

    fun getCardBenefits(card: CreditCard): List<CardBenefit> {
        val benefits = mutableListOf<CardBenefit>()

        // Default cashback
        benefits.add(CardBenefit(
            category = "All categories (default)",
            cashbackPercent = card.defaultCashbackPercent,
            monthlyCap = null,
            note = null
        ))

        // Category-specific rules
        card.rewardRules.forEach { rule ->
            val cap = rule.monthlyCap?.let { formatRupees(it) }
            val note = buildString {
                if (rule.redemptionFee > 0) append("Redemption fee: ${formatRupees(rule.redemptionFee)}. ")
                if (rule.minSpendForReward != null) append("Min spend: ${formatRupees(rule.minSpendForReward)}. ")
                if (rule.forexMarkupPercent > 0) append("Forex: ${rule.forexMarkupPercent}%. ")
            }.takeIf { it.isNotBlank() }

            benefits.add(CardBenefit(
                category = rule.category.displayName,
                cashbackPercent = rule.cashbackPercent,
                monthlyCap = cap?.let { "Cap: $it/mo" },
                note = note
            ))
        }

        // Annual fee info
        if (card.annualFee > 0) {
            benefits.add(CardBenefit(
                category = "Annual Fee",
                cashbackPercent = 0.0,
                monthlyCap = formatRupees(card.annualFee),
                note = card.annualFeeWaiverSpend?.let { "Waived if spend ≥ ${formatRupees(it)}/year" }
            ))
        }

        return benefits
    }

    // ═══════════════════════════════════════════
    //  4. AI Card Advisor (suggest better card)
    // ═══════════════════════════════════════════

    /**
     * Based on the user's actual spend pattern, suggest if another card in their
     * wallet would be better. Rule 12: Never suggest getting a NEW card for spending.
     */
    fun suggestBetterCards(
        cards: List<CreditCard>,
        transactions: List<Transaction>
    ): List<CardSuggestion> {
        if (cards.size < 2) return emptyList() // Need at least 2 cards to compare

        val suggestions = mutableListOf<CardSuggestion>()

        // Group transactions by category
        val spendByCategory = transactions
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        spendByCategory.forEach { (category, totalSpend) ->
            // Find which card was actually used most for this category
            val usedCards = transactions.filter { it.category == category }
                .groupBy { it.cardId }
                .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            val mostUsedCardId = usedCards.maxByOrNull { it.value }?.key

            // Find the best card for this category
            val bestCard = cards.maxByOrNull { card ->
                val rule = card.rewardRules.find { it.category == category }
                rule?.netSavingsPercent ?: card.defaultCashbackPercent
            }

            if (bestCard != null && mostUsedCardId != null && bestCard.id != mostUsedCardId) {
                val bestRate = bestCard.rewardRules
                    .find { it.category == category }?.netSavingsPercent
                    ?: bestCard.defaultCashbackPercent

                val currentCard = cards.find { it.id == mostUsedCardId }
                val currentRate = currentCard?.rewardRules
                    ?.find { it.category == category }?.netSavingsPercent
                    ?: (currentCard?.defaultCashbackPercent ?: 0.0)

                val extraSavings = totalSpend * (bestRate - currentRate) / 100.0

                if (extraSavings > 10) { // Only suggest if savings > ₹10
                    suggestions.add(CardSuggestion(
                        suggestedCardName = bestCard.cardName,
                        reason = "Use ${bestCard.cardName} for ${category.displayName} instead — ${bestRate}% vs ${currentRate}%",
                        potentialExtraSavings = extraSavings,
                        forCategory = category
                    ))
                }
            }
        }

        return suggestions.sortedByDescending { it.potentialExtraSavings }
    }

    // ═══════════════════════════════════════════
    //  Private Helpers
    // ═══════════════════════════════════════════

    private fun calculatePointsEarned(card: CreditCard, transactions: List<Transaction>): Int {
        var total = 0
        transactions.forEach { txn ->
            val rule = card.rewardRules.find { it.category == txn.category }
            val ratePercent = rule?.cashbackPercent ?: card.defaultCashbackPercent
            total += (txn.amount * ratePercent / 100).toInt()
        }
        return total
    }

    /**
     * Per-issuer point valuation (Indian market).
     * Rule 13: Factor in redemption fees.
     */
    private fun calculatePointValue(issuer: CardIssuer, pendingPoints: Int): Double {
        val pointValuePaise = when (issuer) {
            CardIssuer.HDFC -> 0.50    // 1 pt = ₹0.50
            CardIssuer.SBI -> 0.25     // 1 pt = ₹0.25
            CardIssuer.ICICI -> 0.25
            CardIssuer.AXIS -> 0.40
            CardIssuer.KOTAK -> 0.20
            CardIssuer.AMEX -> 0.50
            CardIssuer.YES -> 0.25
            CardIssuer.RBL -> 0.25
            CardIssuer.IDFC -> 0.30
            CardIssuer.AU -> 0.20
            CardIssuer.HSBC -> 0.35
            CardIssuer.CITI -> 0.30
            CardIssuer.SC -> 0.25
            CardIssuer.INDUSIND -> 0.30
            CardIssuer.BOB -> 0.20
            else -> 0.25
        }
        return pendingPoints * pointValuePaise
    }

    private fun formatRupees(amount: Double): String {
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return fmt.format(amount).replace(".00", "")
    }
}
