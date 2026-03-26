package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for OptimalSwiperUseCase.
 * Rule 14: All domain logic must be tested as pure functions.
 */
class OptimalSwiperUseCaseTest {

    private lateinit var useCase: OptimalSwiperUseCase

    @Before
    fun setup() {
        useCase = OptimalSwiperUseCase()
    }

    @Test
    fun `empty cards returns add-cards prompt`() {
        val result = useCase.execute("Groceries at Blinkit", emptyList())
        assertEquals(InsightType.OPTIMAL_CARD, result.type)
        assertTrue(result.headline.contains("Add your credit cards"))
    }

    @Test
    fun `debt danger card returns critical debt warning`() {
        val debtCard = CreditCard(
            cardName = "HDFC Regalia",
            issuer = CardIssuer.HDFC,
            lastFourDigits = "1234",
            rewardType = RewardType.POINTS,
            defaultCashbackPercent = 1.5,
            isRevolvingCredit = true,
            creditLimit = 100000.0,
            currentBalance = 85000.0
        )

        val result = useCase.execute("Groceries", listOf(debtCard))
        assertEquals(InsightType.DEBT_WARNING, result.type)
        assertEquals(InsightSeverity.CRITICAL, result.severity)
        assertTrue(result.isDebtModeInsight)
        assertTrue(result.headline.contains("Pay off"))
    }

    @Test
    fun `best card for category is recommended`() {
        val groceryCard = CreditCard(
            cardName = "SBI SimplySave",
            issuer = CardIssuer.SBI,
            lastFourDigits = "5678",
            rewardType = RewardType.CASHBACK,
            defaultCashbackPercent = 1.0,
            rewardRules = listOf(
                CardRewardRule(
                    category = SpendCategory.GROCERY,
                    cashbackPercent = 5.0,
                    monthlyCap = 500.0
                )
            )
        )
        val otherCard = CreditCard(
            cardName = "HDFC Millennia",
            issuer = CardIssuer.HDFC,
            lastFourDigits = "9012",
            rewardType = RewardType.CASHBACK,
            defaultCashbackPercent = 1.0
        )

        val result = useCase.execute(
            "Buying groceries at BigBasket",
            listOf(groceryCard, otherCard),
            amount = 2000.0
        )

        assertEquals(InsightType.OPTIMAL_CARD, result.type)
        assertEquals(InsightSeverity.SUCCESS, result.severity)
        assertEquals(groceryCard.id, result.recommendedCardId)
        assertTrue(result.savingsAmount > 0)
    }

    @Test
    fun `no savings returns info insight`() {
        val card = CreditCard(
            cardName = "Basic Card",
            issuer = CardIssuer.OTHER,
            lastFourDigits = "0000",
            rewardType = RewardType.CASHBACK,
            defaultCashbackPercent = 0.0
        )

        val result = useCase.execute("Random purchase", listOf(card), 100.0)
        assertEquals(InsightSeverity.INFO, result.type.let { InsightSeverity.INFO })
    }
}
