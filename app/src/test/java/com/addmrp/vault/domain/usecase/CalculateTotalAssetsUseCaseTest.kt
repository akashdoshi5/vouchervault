package com.addmrp.vault.domain.usecase

import com.addmrp.vault.domain.model.RedemptionSource
import com.addmrp.vault.domain.model.Voucher
import com.addmrp.vault.domain.model.VoucherCategory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for CalculateTotalAssetsUseCase.
 * Tests the pure business logic independently of Android (Rule 5).
 */
class CalculateTotalAssetsUseCaseTest {

    private val useCase = CalculateTotalAssetsUseCase()

    private fun createVoucher(
        value: Double,
        isRedeemed: Boolean = false,
        expiresInHours: Long = 72
    ): Voucher = Voucher(
        id = "test-${System.nanoTime()}",
        brand = "Test Brand",
        category = VoucherCategory.FOOD,
        code = "TEST123",
        value = value,
        source = RedemptionSource.GPAY,
        expiryUtc = Instant.now().plus(expiresInHours, ChronoUnit.HOURS),
        isRedeemed = isRedeemed
    )

    @Test
    fun `sum only active vouchers`() {
        val vouchers = listOf(
            createVoucher(value = 100.0),
            createVoucher(value = 200.0),
            createVoucher(value = 50.0, isRedeemed = true),   // Excluded
            createVoucher(value = 75.0, expiresInHours = -1)  // Expired — Excluded
        )

        val total = useCase(vouchers)

        assertEquals(300.0, total, 0.01)
    }

    @Test
    fun `returns zero for empty list`() {
        assertEquals(0.0, useCase(emptyList()), 0.01)
    }

    @Test
    fun `returns zero when all expired`() {
        val vouchers = listOf(
            createVoucher(value = 500.0, expiresInHours = -1),
            createVoucher(value = 300.0, expiresInHours = -24)
        )
        assertEquals(0.0, useCase(vouchers), 0.01)
    }

    @Test
    fun `returns zero when all redeemed`() {
        val vouchers = listOf(
            createVoucher(value = 100.0, isRedeemed = true),
            createVoucher(value = 200.0, isRedeemed = true)
        )
        assertEquals(0.0, useCase(vouchers), 0.01)
    }
}
