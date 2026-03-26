package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FrugalityFilter.
 * Rule 12: Verifies spending-encouragement language is stripped.
 * Rule 16: Verifies debt mode override.
 */
class FrugalityFilterTest {

    private lateinit var filter: FrugalityFilter

    @Before
    fun setup() {
        filter = FrugalityFilter()
    }

    @Test
    fun `filter strips spending trigger words`() {
        val insight = AdvisorInsight(
            type = InsightType.OPTIMAL_CARD,
            severity = InsightSeverity.SUCCESS,
            headline = "Buy more to unlock rewards by spending",
            description = "Spend more at Amazon to earn rewards."
        )

        val filtered = filter.filter(insight)
        assertFalse(filtered.headline.contains("buy more", ignoreCase = true))
        assertFalse(filtered.description.contains("spend more", ignoreCase = true))
    }

    @Test
    fun `filter reframes reward language to savings language`() {
        val insight = AdvisorInsight(
            type = InsightType.OPTIMAL_CARD,
            severity = InsightSeverity.SUCCESS,
            headline = "Use HDFC to earn points",
            description = "Collect rewards on your Swiggy orders."
        )

        val filtered = filter.filter(insight)
        assertTrue(filtered.headline.contains("save money", ignoreCase = true))
        assertFalse(filtered.headline.contains("earn points", ignoreCase = true))
    }

    @Test
    fun `debt mode overrides all non-debt insights`() {
        val insight = AdvisorInsight(
            type = InsightType.OPTIMAL_CARD,
            severity = InsightSeverity.SUCCESS,
            headline = "Great savings available!",
            description = "Use your HDFC card for 5% cashback."
        )

        val filtered = filter.filter(insight, debtModeActive = true)
        assertEquals(InsightType.DEBT_WARNING, filtered.type)
        assertEquals(InsightSeverity.CRITICAL, filtered.severity)
        assertTrue(filtered.isDebtModeInsight)
    }

    @Test
    fun `debt mode does not override existing debt insights`() {
        val debtInsight = AdvisorInsight(
            type = InsightType.DEBT_WARNING,
            severity = InsightSeverity.CRITICAL,
            headline = "Pay off your card",
            description = "Clear the balance.",
            isDebtModeInsight = true
        )

        val filtered = filter.filter(debtInsight, debtModeActive = true)
        assertEquals("Pay off your card", filtered.headline)
    }

    @Test
    fun `isSafe returns false for triggered content`() {
        val unsafe = AdvisorInsight(
            type = InsightType.OPTIMAL_CARD,
            severity = InsightSeverity.SUCCESS,
            headline = "Don't miss out on rewards",
            description = "Shop now to maximize."
        )
        assertFalse(filter.isSafe(unsafe))
    }

    @Test
    fun `isSafe returns true for clean content`() {
        val safe = AdvisorInsight(
            type = InsightType.OPTIMAL_CARD,
            severity = InsightSeverity.SUCCESS,
            headline = "Use HDFC for 5% cashback",
            description = "Save ₹100 on groceries."
        )
        assertTrue(filter.isSafe(safe))
    }
}
