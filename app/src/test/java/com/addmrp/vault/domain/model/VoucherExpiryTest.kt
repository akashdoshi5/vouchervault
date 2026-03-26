package com.addmrp.vault.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for Voucher domain model expiry logic.
 * Pure JVM tests — no Android dependencies (Rule 5).
 */
class VoucherExpiryTest {

    @Test
    fun `isExpired returns true when past expiry`() {
        val voucher = Voucher(
            expiryUtc = Instant.now().minus(1, ChronoUnit.HOURS)
        )
        assertTrue(voucher.isExpired)
    }

    @Test
    fun `isExpired returns false when before expiry`() {
        val voucher = Voucher(
            expiryUtc = Instant.now().plus(24, ChronoUnit.HOURS)
        )
        assertFalse(voucher.isExpired)
    }

    @Test
    fun `isExpiringSoon returns true when less than 24h left`() {
        val voucher = Voucher(
            expiryUtc = Instant.now().plus(12, ChronoUnit.HOURS)
        )
        assertTrue(voucher.isExpiringSoon)
    }

    @Test
    fun `isExpiringSoon returns false when more than 24h left`() {
        val voucher = Voucher(
            expiryUtc = Instant.now().plus(48, ChronoUnit.HOURS)
        )
        assertFalse(voucher.isExpiringSoon)
    }

    @Test
    fun `isExpiringSoon returns false when expired`() {
        val voucher = Voucher(
            expiryUtc = Instant.now().minus(1, ChronoUnit.HOURS)
        )
        assertFalse(voucher.isExpiringSoon) // Not "soon" — it's already expired
    }

    @Test
    fun `expiryCountdownText shows EXPIRED for past expiry`() {
        val voucher = Voucher(
            expiryUtc = Instant.now().minus(1, ChronoUnit.HOURS)
        )
        assertEquals("EXPIRED", voucher.expiryCountdownText())
    }

    @Test
    fun `expiryCountdownText shows days for more than 24h`() {
        val voucher = Voucher(
            expiryUtc = Instant.now().plus(72, ChronoUnit.HOURS)
        )
        assertTrue(voucher.expiryCountdownText().startsWith("Expires in 3"))
    }

    @Test
    fun `expiryCountdownText shows hours for less than 24h`() {
        val voucher = Voucher(
            expiryUtc = Instant.now().plus(12, ChronoUnit.HOURS)
        )
        assertTrue(voucher.expiryCountdownText().startsWith("Expiring in"))
    }
}
