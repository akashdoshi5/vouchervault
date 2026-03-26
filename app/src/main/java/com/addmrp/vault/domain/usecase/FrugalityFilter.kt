package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.AdvisorInsight
import com.addmrp.vault.domain.model.InsightSeverity
import com.addmrp.vault.domain.model.InsightType
import javax.inject.Inject

/**
 * Frugality Filter — the ethical guardrail for all AI advice.
 *
 * PURE FUNCTION: (insight, debtModeActive) → filtered AdvisorInsight
 *
 * Rule 12: Every AdvisorInsight must pass through this filter before reaching the UI.
 * Rule 16: If debtModeActive, all reward insights are replaced with debt warnings.
 * Rule 14: Must be unit tested to verify spending-encouragement stripping.
 */
class FrugalityFilter @Inject constructor() {

    // Words/phrases that encourage spending — must be stripped or reframed
    private val spendingTriggerWords = listOf(
        "buy more", "spend more", "increase spending", "upgrade your",
        "unlock rewards by spending", "hit the target", "reach the milestone",
        "treat yourself", "you deserve", "why not try", "maximize by purchasing",
        "add to cart", "shop now", "don't miss out", "limited time offer"
    )

    /**
     * Filter a single insight.
     * Returns the insight with spending-encouragement language removed
     * and debt mode overrides applied.
     */
    fun filter(insight: AdvisorInsight, debtModeActive: Boolean = false): AdvisorInsight {
        // Rule 16: Debt mode overrides everything
        if (debtModeActive && !insight.isDebtModeInsight) {
            return insight.copy(
                type = InsightType.DEBT_WARNING,
                severity = InsightSeverity.CRITICAL,
                headline = "Focus on debt payoff",
                description = "Reward optimization is paused while you have revolving credit. " +
                        "Clear your outstanding balance first — interest charges far exceed any rewards.",
                isDebtModeInsight = true
            )
        }

        // Strip spending-encouragement language
        var sanitizedHeadline = insight.headline
        var sanitizedDescription = insight.description

        spendingTriggerWords.forEach { trigger ->
            sanitizedHeadline = sanitizedHeadline.replace(trigger, "", ignoreCase = true)
            sanitizedDescription = sanitizedDescription.replace(trigger, "", ignoreCase = true)
        }

        // Reframe from "rewards" to "savings" language
        sanitizedHeadline = reframeSavingsLanguage(sanitizedHeadline)
        sanitizedDescription = reframeSavingsLanguage(sanitizedDescription)

        return insight.copy(
            headline = sanitizedHeadline.trim(),
            description = sanitizedDescription.trim()
        )
    }

    /**
     * Filter a list of insights.
     * Convenience method for batch processing.
     */
    fun filterAll(
        insights: List<AdvisorInsight>,
        debtModeActive: Boolean = false
    ): List<AdvisorInsight> {
        return insights.map { filter(it, debtModeActive) }
    }

    /**
     * Validate that an insight doesn't contain spending triggers.
     * Returns true if the insight is safe — used in unit tests.
     */
    fun isSafe(insight: AdvisorInsight): Boolean {
        val fullText = "${insight.headline} ${insight.description}".lowercase()
        return spendingTriggerWords.none { fullText.contains(it) }
    }

    private fun reframeSavingsLanguage(text: String): String {
        return text
            .replace("earn points", "save money", ignoreCase = true)
            .replace("earn rewards", "save money", ignoreCase = true)
            .replace("get points", "save money", ignoreCase = true)
            .replace("collect rewards", "save money", ignoreCase = true)
            .replace("accumulate points", "maximize savings", ignoreCase = true)
            .replace("rack up rewards", "maximize savings", ignoreCase = true)
    }
}
