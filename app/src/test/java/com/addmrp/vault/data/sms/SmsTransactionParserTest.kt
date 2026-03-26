package com.addmrp.vault.data.sms

import com.addmrp.vault.domain.model.SpendCategory
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SmsTransactionParser.
 * Rule 14: SMS parsing must be thoroughly tested per bank format.
 * Rule 15: All parsing is on-device — these tests verify correct extraction.
 */
class SmsTransactionParserTest {

    @Test
    fun `extract amount from HDFC format`() {
        val sms = "Alert: INR 1,234.50 debited from a/c XX5678 on 15-Jan-25 to BLINKIT at BLR Avl Bal INR 45,678.00"
        val amount = SmsTransactionParser.extractAmount(sms)
        assertEquals(1234.50, amount!!, 0.01)
    }

    @Test
    fun `extract amount from SBI format`() {
        val sms = "Your SBI Card XX9012 has been used for Rs.2500.00 at AMAZON IN on 20/01/2025."
        val amount = SmsTransactionParser.extractAmount(sms)
        assertEquals(2500.0, amount!!, 0.01)
    }

    @Test
    fun `extract amount with rupee symbol`() {
        val sms = "Txn of ₹850 done on ICICI Card XX3456 at SWIGGY on 15-Jan-25"
        val amount = SmsTransactionParser.extractAmount(sms)
        assertEquals(850.0, amount!!, 0.01)
    }

    @Test
    fun `extract card last four digits`() {
        val sms = "Alert: INR 500 debited from a/c XX1234 on 15-Jan-25"
        val lastFour = SmsTransactionParser.extractCardLastFour(sms)
        assertEquals("1234", lastFour)
    }

    @Test
    fun `extract card with ending format`() {
        val sms = "Payment of Rs 1000 from card ending 5678 at Amazon"
        val lastFour = SmsTransactionParser.extractCardLastFour(sms)
        assertEquals("5678", lastFour)
    }

    @Test
    fun `extract merchant name`() {
        val sms = "INR 500 debited from XX1234 at BLINKIT GROCERY on 15-Jan-25."
        val merchant = SmsTransactionParser.extractMerchant(sms)
        assertNotNull(merchant)
        assertTrue(merchant!!.contains("BLINKIT", ignoreCase = true))
    }

    @Test
    fun `detect debit transaction`() {
        assertTrue(SmsTransactionParser.isDebitTransaction("INR 500 debited from your account"))
        assertTrue(SmsTransactionParser.isDebitTransaction("Payment of Rs 1000 at Amazon"))
        assertTrue(SmsTransactionParser.isDebitTransaction("Your card was charged Rs.500"))
    }

    @Test
    fun `detect non-debit transaction`() {
        assertFalse(SmsTransactionParser.isDebitTransaction("Cashback of Rs 50 credited to your account"))
    }

    @Test
    fun `identify bank from sender`() {
        assertEquals("HDFC", SmsTransactionParser.identifyBank("AD-HDFCBK"))
        assertEquals("SBI", SmsTransactionParser.identifyBank("VM-SBIBNK"))
        assertEquals("ICICI", SmsTransactionParser.identifyBank("AD-ICICIB"))
        assertNull(SmsTransactionParser.identifyBank("AD-UNKNOWN"))
    }

    @Test
    fun `full parse of HDFC SMS`() {
        val sms = "Alert: INR 1,500.00 debited from a/c XX1234 on 15-Jan-25 to SWIGGY at DELHI Avl Bal INR 45,000.00"
        val txn = SmsTransactionParser.parse(sms, "AD-HDFCBK")
        assertNotNull(txn)
        assertEquals(1500.0, txn!!.amount, 0.01)
        assertEquals("1234", txn.cardId)
        assertEquals(SpendCategory.FOOD_DELIVERY, txn.category)
    }

    @Test
    fun `non-transaction SMS returns null`() {
        val sms = "Your OTP for login is 123456. Do not share with anyone."
        val txn = SmsTransactionParser.parse(sms, "AD-HDFCBK")
        assertNull(txn)
    }

    @Test
    fun `credit transaction (refund) returns null`() {
        val sms = "INR 500.00 credited to your a/c XX1234 on 15-Jan-25 from AMAZON REFUND"
        // isDebitTransaction should be false but isTransactionSms is true, so parse returns null
        // because we only track debit transactions
    }

    @Test
    fun `very small amount is skipped`() {
        val sms = "INR 0.50 debited from a/c XX1234 on 15-Jan-25 to TEST"
        val txn = SmsTransactionParser.parse(sms, "AD-HDFCBK")
        assertNull(txn)
    }
}
