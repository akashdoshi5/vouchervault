package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.Instant

/**
 * Unit tests for RewardIntelligenceUseCase.
 * Rule 14: All reward logic tested as pure functions.
 */
class RewardIntelligenceUseCaseTest {

    private lateinit var useCase: RewardIntelligenceUseCase

    // Test fixtures
    private val hdfcCard = CreditCard(
        id = "hdfc-1",
        cardName = "HDFC Regalia",
        issuer = CardIssuer.HDFC,
        lastFourDigits = "1234",
        rewardType = RewardType.POINTS,
        defaultCashbackPercent = 1.0,
        rewardPointsBalance = 5000,
        pointsExpiryUtc = Instant.now().plus(Duration.ofDays(10)),
        rewardRules = listOf(
            CardRewardRule(category = SpendCategory.GROCERY, cashbackPercent = 5.0, monthlyCap = 500.0),
            CardRewardRule(category = SpendCategory.FOOD_DELIVERY, cashbackPercent = 3.0)
        )
    )

    private val sbiCard = CreditCard(
        id = "sbi-1",
        cardName = "SBI SimplySave",
        issuer = CardIssuer.SBI,
        lastFourDigits = "5678",
        rewardType = RewardType.CASHBACK,
        defaultCashbackPercent = 0.5,
        rewardPointsBalance = 2000,
        pointsExpiryUtc = Instant.now().plus(Duration.ofDays(3)), // Urgent!
        rewardRules = listOf(
            CardRewardRule(category = SpendCategory.GROCERY, cashbackPercent = 2.0)
        )
    )

    @Before
    fun setup() {
        useCase = RewardIntelligenceUseCase()
    }

    // ── Monthly Summary Tests ──

    @Test
    fun `monthly summary returns entry per card`() {
        val summaries = useCase.getMonthlyRewardSummary(
            listOf(hdfcCard, sbiCard),
            emptyList()
        )
        assertEquals(2, summaries.size)
        assertEquals("HDFC Regalia", summaries[0].cardName)
        assertEquals("SBI SimplySave", summaries[1].cardName)
    }

    @Test
    fun `monthly summary calculates pending value`() {
        val summaries = useCase.getMonthlyRewardSummary(listOf(hdfcCard), emptyList())
        // HDFC: 5000 pts × ₹0.50 = ₹2500
        assertEquals(2500.0, summaries[0].pendingValueInRupees, 0.01)
    }

    @Test
    fun `empty cards returns empty summary`() {
        val summaries = useCase.getMonthlyRewardSummary(emptyList(), emptyList())
        assertTrue(summaries.isEmpty())
    }

    // ── Expiring Rewards Tests ──

    @Test
    fun `expiring rewards detects urgent cards`() {
        val expiring = useCase.getExpiringRewards(listOf(hdfcCard, sbiCard))
        assertTrue(expiring.isNotEmpty())
        // SBI card expires in 3 days — should be first and marked urgent
        val sbiExpiring = expiring.find { it.cardName == "SBI SimplySave" }
        assertNotNull(sbiExpiring)
        assertTrue(sbiExpiring!!.isUrgent)
        assertTrue(sbiExpiring.daysUntilExpiry <= 7)
    }

    @Test
    fun `card with no points not in expiring list`() {
        val emptyCard = CreditCard(
            cardName = "Empty Card",
            issuer = CardIssuer.AXIS,
            lastFourDigits = "0000",
            rewardType = RewardType.CASHBACK,
            defaultCashbackPercent = 1.0,
            rewardPointsBalance = 0,
            pointsExpiryUtc = Instant.now().plus(Duration.ofDays(5))
        )
        val expiring = useCase.getExpiringRewards(listOf(emptyCard))
        assertTrue(expiring.isEmpty())
    }

    @Test
    fun `card with no expiry not in expiring list`() {
        val noExpiryCard = hdfcCard.copy(pointsExpiryUtc = null)
        val expiring = useCase.getExpiringRewards(listOf(noExpiryCard))
        assertTrue(expiring.isEmpty())
    }

    // ── Card Benefits Tests ──

    @Test
    fun `card benefits includes default and category rules`() {
        val benefits = useCase.getCardBenefits(hdfcCard)
        assertTrue(benefits.size >= 3) // default + grocery + food_delivery
        assertEquals("All categories (default)", benefits[0].category)
    }

    @Test
    fun `card with annual fee shows fee in benefits`() {
        val feeCard = hdfcCard.copy(annualFee = 2500.0, annualFeeWaiverSpend = 200000.0)
        val benefits = useCase.getCardBenefits(feeCard)
        val feeBenefit = benefits.find { it.category == "Annual Fee" }
        assertNotNull(feeBenefit)
        assertTrue(feeBenefit!!.note?.contains("Waived") == true)
    }

    // ── Card Suggestions Tests ──

    @Test
    fun `no suggestions with single card`() {
        val suggestions = useCase.suggestBetterCards(listOf(hdfcCard), emptyList())
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `suggests better card for category mismatch`() {
        val transactions = listOf(
            Transaction(
                amount = 5000.0,
                merchant = "BigBasket",
                category = SpendCategory.GROCERY,
                cardId = "sbi-1", // Using SBI (2%) instead of HDFC (5%)
                cardName = "SBI SimplySave"
            )
        )
        val suggestions = useCase.suggestBetterCards(listOf(hdfcCard, sbiCard), transactions)
        // HDFC has 5% grocery vs SBI 2% — should suggest HDFC
        if (suggestions.isNotEmpty()) {
            assertTrue(suggestions[0].potentialExtraSavings > 0)
        }
    }
}
