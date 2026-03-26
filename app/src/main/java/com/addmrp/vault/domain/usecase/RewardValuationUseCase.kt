package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.CreditCard
import com.addmrp.vault.domain.model.ValuationTier
import javax.inject.Inject

/**
 * Reward Point Valuation Engine.
 *
 * PURE FUNCTION: (points, issuer) → List<ValuationTier>
 *
 * Rule 13: Deducts redemption fees from each tier's net value.
 * Rule 14: 100% testable — no Android dependencies.
 */
class RewardValuationUseCase @Inject constructor() {

    /**
     * Calculate the monetary value of reward points across different
     * redemption channels for a given card issuer.
     *
     * Returns tiers sorted by net value (best first).
     */
    fun execute(points: Int, card: CreditCard): List<ValuationTier> {
        if (points <= 0) return emptyList()

        val tiers = getValuationTiers(card)
            .map { tier ->
                val totalValue = points * tier.valuePerPoint
                val netValue = (totalValue - tier.fee).coerceAtLeast(0.0)
                tier.copy(
                    totalValue = totalValue,
                    netValue = netValue
                )
            }
            .sortedByDescending { it.netValue }

        // Mark the best value
        return tiers.mapIndexed { index, tier ->
            tier.copy(isBestValue = index == 0 && tier.netValue > 0)
        }
    }

    /**
     * Get the best single ₹ value for a given point balance and card.
     * Convenience method for dashboard display.
     */
    fun getBestValue(points: Int, card: CreditCard): Double {
        return execute(points, card).firstOrNull()?.netValue ?: 0.0
    }

    /**
     * Get valuation tiers for Indian card issuers.
     * Data sourced from public issuer redemption catalogs.
     *
     * Note: These are approximate mid-2025 values. Update periodically.
     */
    private fun getValuationTiers(card: CreditCard): List<ValuationTier> {
        // Standard redemption fee for most Indian banks
        val standardFee = 116.82 // ₹99 + 18% GST

        return when (card.issuer) {
            com.addmrp.vault.domain.model.CardIssuer.HDFC -> listOf(
                ValuationTier("SmartBuy (Flights/Hotels)", 1.0, 0.0, 0.0, 0.0),
                ValuationTier("Brand Vouchers (Tanishq/Amazon)", 0.50, 0.0, standardFee, 0.0),
                ValuationTier("Statement Credit", 0.30, 0.0, standardFee, 0.0),
                ValuationTier("Catalogue Products", 0.20, 0.0, standardFee, 0.0)
            )
            com.addmrp.vault.domain.model.CardIssuer.SBI -> listOf(
                ValuationTier("Statement Credit", 0.25, 0.0, standardFee, 0.0),
                ValuationTier("Amazon Voucher", 0.25, 0.0, standardFee, 0.0),
                ValuationTier("Catalogue Products", 0.15, 0.0, standardFee, 0.0)
            )
            com.addmrp.vault.domain.model.CardIssuer.ICICI -> listOf(
                ValuationTier("PayBack Catalog", 0.25, 0.0, 0.0, 0.0),
                ValuationTier("Amazon/Flipkart Vouchers", 0.25, 0.0, standardFee, 0.0),
                ValuationTier("Statement Credit", 0.20, 0.0, standardFee, 0.0)
            )
            com.addmrp.vault.domain.model.CardIssuer.AXIS -> listOf(
                ValuationTier("Edge Rewards Catalog", 0.40, 0.0, 0.0, 0.0),
                ValuationTier("Brand Vouchers", 0.33, 0.0, standardFee, 0.0),
                ValuationTier("Statement Credit", 0.25, 0.0, standardFee, 0.0)
            )
            com.addmrp.vault.domain.model.CardIssuer.AMEX -> listOf(
                ValuationTier("Amex Travel Portal", 0.50, 0.0, 0.0, 0.0),
                ValuationTier("Hotel/Flight Transfers", 0.50, 0.0, 0.0, 0.0),
                ValuationTier("Statement Credit", 0.30, 0.0, 0.0, 0.0),
                ValuationTier("Retail Vouchers", 0.25, 0.0, standardFee, 0.0)
            )
            com.addmrp.vault.domain.model.CardIssuer.KOTAK -> listOf(
                ValuationTier("PVR/BookMyShow Vouchers", 0.25, 0.0, 0.0, 0.0),
                ValuationTier("Statement Credit", 0.20, 0.0, standardFee, 0.0)
            )
            com.addmrp.vault.domain.model.CardIssuer.IDFC -> listOf(
                ValuationTier("Statement Credit", 0.25, 0.0, 0.0, 0.0),
                ValuationTier("Amazon Voucher", 0.25, 0.0, 0.0, 0.0)
            )
            else -> listOf(
                ValuationTier("Statement Credit", 0.20, 0.0, standardFee, 0.0),
                ValuationTier("Vouchers", 0.15, 0.0, standardFee, 0.0)
            )
        }
    }
}
