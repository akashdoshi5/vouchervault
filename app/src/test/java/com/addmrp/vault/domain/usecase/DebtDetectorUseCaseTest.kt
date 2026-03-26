package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DebtDetectorUseCase.
 * Rule 16: Verifies revolving credit and high utilization detection.
 */
class DebtDetectorUseCaseTest {

    private lateinit var useCase: DebtDetectorUseCase

    @Before
    fun setup() {
        useCase = DebtDetectorUseCase()
    }

    @Test
    fun `empty cards returns HEALTHY`() {
        assertEquals(DebtStatus.HEALTHY, useCase.execute(emptyList()))
    }

    @Test
    fun `no debt cards returns HEALTHY`() {
        val card = CreditCard(
            cardName = "Safe Card",
            issuer = CardIssuer.HDFC,
            lastFourDigits = "1234",
            rewardType = RewardType.CASHBACK,
            defaultCashbackPercent = 1.0,
            isRevolvingCredit = false,
            creditLimit = 100000.0,
            currentBalance = 20000.0  // 20% utilization
        )
        assertEquals(DebtStatus.HEALTHY, useCase.execute(listOf(card)))
    }

    @Test
    fun `revolving credit returns CRITICAL`() {
        val card = CreditCard(
            cardName = "Risky Card",
            issuer = CardIssuer.SBI,
            lastFourDigits = "5678",
            rewardType = RewardType.CASHBACK,
            defaultCashbackPercent = 1.0,
            isRevolvingCredit = true,
            creditLimit = 50000.0,
            currentBalance = 45000.0
        )

        val result = useCase.execute(listOf(card))
        assertTrue(result is DebtStatus.CRITICAL)
        val critical = result as DebtStatus.CRITICAL
        assertEquals(45000.0, critical.totalOutstanding, 0.01)
        assertTrue(critical.estimatedMonthlyInterest > 0)
        assertTrue(critical.recommendation.contains("URGENT"))
    }

    @Test
    fun `high utilization returns WARNING`() {
        val card = CreditCard(
            cardName = "High Util Card",
            issuer = CardIssuer.ICICI,
            lastFourDigits = "9012",
            rewardType = RewardType.POINTS,
            defaultCashbackPercent = 1.0,
            isRevolvingCredit = false,
            creditLimit = 100000.0,
            currentBalance = 80000.0  // 80% utilization
        )

        val result = useCase.execute(listOf(card))
        assertTrue(result is DebtStatus.WARNING)
        val warning = result as DebtStatus.WARNING
        assertTrue(warning.avgUtilization > 70)
    }

    @Test
    fun `isDebtModeActive returns true for revolving credit`() {
        val card = CreditCard(
            cardName = "Debt Card",
            issuer = CardIssuer.AXIS,
            lastFourDigits = "3456",
            rewardType = RewardType.CASHBACK,
            defaultCashbackPercent = 1.0,
            isRevolvingCredit = true
        )
        assertTrue(useCase.isDebtModeActive(listOf(card)))
    }

    @Test
    fun `isDebtModeActive returns false for healthy cards`() {
        val card = CreditCard(
            cardName = "Healthy Card",
            issuer = CardIssuer.KOTAK,
            lastFourDigits = "7890",
            rewardType = RewardType.CASHBACK,
            defaultCashbackPercent = 1.0,
            isRevolvingCredit = false,
            creditLimit = 100000.0,
            currentBalance = 10000.0
        )
        assertFalse(useCase.isDebtModeActive(listOf(card)))
    }
}
