package com.addmrp.vault.data.sms

import com.addmrp.vault.domain.model.SpendCategory
import com.addmrp.vault.domain.model.Transaction
import com.addmrp.vault.domain.model.TransactionSource
import java.time.Instant
import java.util.UUID

/**
 * On-device SMS parser for Indian bank transaction messages.
 *
 * Rule 15: ALL processing happens on-device — never sends SMS data to cloud.
 * Rule 14: Pure function — (smsBody, sender) → Transaction?
 *          Must have per-bank unit tests.
 *
 * Supports: HDFC, SBI, ICICI, Axis, Kotak, IDFC, RBL, IndusInd, Amex, Yes Bank.
 */
object SmsTransactionParser {

    // ═══════════════════════════════════════════
    // Regex patterns for Indian bank SMS formats
    // ═══════════════════════════════════════════

    // Amount: INR 1,234.56 or Rs.1234.56 or Rs 1,234 or ₹1234
    private val AMOUNT_REGEX = Regex(
        """(?:INR|Rs\.?|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // Card last 4 digits: XX1234 or x1234 or **1234 or ****1234 or ending 1234
    private val CARD_REGEX = Regex(
        """(?:[Xx*]{2,}|ending\s*|ending with\s*)(\d{4})""",
        RegexOption.IGNORE_CASE
    )

    // Merchant: at <merchant> on or at <merchant>.
    private val MERCHANT_REGEX = Regex(
        """(?:at|to|towards|for|@)\s+([A-Za-z0-9\s&'.\-/]+?)(?:\s+on|\s+dt|\.|,|\s+ref|\s+Avl)""",
        RegexOption.IGNORE_CASE
    )

    // Date: dd-MM-yy or dd/MM/yyyy or dd-MMM-yy
    private val DATE_REGEX = Regex(
        """(\d{1,2}[-/]\d{1,2}[-/]\d{2,4}|\d{1,2}-[A-Za-z]{3}-\d{2,4})""",
        RegexOption.IGNORE_CASE
    )

    // Transaction type detection
    private val DEBIT_KEYWORDS = listOf(
        "debited", "spent", "purchase", "payment", "withdrawn",
        "txn", "transaction", "paid", "deducted", "charged"
    )
    private val CREDIT_KEYWORDS = listOf(
        "credited", "received", "refund", "cashback", "reversal", "deposit"
    )

    // Bank sender ID patterns
    private val BANK_SENDERS = mapOf(
        "HDFCBK" to "HDFC", "SBIBNK" to "SBI", "ICICIB" to "ICICI",
        "AXISBK" to "AXIS", "KOTAKB" to "KOTAK", "IDFCFB" to "IDFC",
        "RBLBNK" to "RBL", "INDUSB" to "INDUSIND", "AMEXIN" to "AMEX",
        "YESBK" to "YES", "FEDERL" to "FEDERAL", "AUBSFB" to "AU",
        "BOBIN" to "BOB"
    )

    /**
     * Parse a single SMS into a Transaction.
     * Returns null if SMS is not a transaction or parsing fails.
     *
     * @param smsBody The full SMS text content
     * @param sender The SMS sender ID (e.g., "AD-HDFCBK")
     * @return Parsed Transaction or null
     */
    fun parse(smsBody: String, sender: String): Transaction? {
        // Skip if not a bank transaction SMS
        if (!isTransactionSms(smsBody)) return null

        // Extract amount
        val amount = extractAmount(smsBody) ?: return null

        // Skip very small transactions (likely OTP or test)
        if (amount < 1.0) return null

        // Extract card last 4
        val lastFour = extractCardLastFour(smsBody)

        // Extract merchant
        val merchant = extractMerchant(smsBody)?.trim() ?: "Unknown Merchant"

        // Detect if debit/credit
        val isDebit = isDebitTransaction(smsBody)

        // Only process debit transactions for spend tracking
        if (!isDebit) return null

        // Auto-detect category from merchant
        val category = SpendCategory.fromMerchant(merchant)

        return Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            merchant = merchant,
            category = category,
            cardId = lastFour ?: "unknown",
            cardName = "",
            isDebitCard = false, // Will be matched to card in repository
            isInternational = false,
            source = TransactionSource.SMS,
            transactionDateUtc = Instant.now(),
            createdAtUtc = Instant.now()
        )
    }

    /**
     * Parse multiple SMS messages.
     * Convenience for batch processing SMS history.
     */
    fun parseBatch(messages: List<Pair<String, String>>): List<Transaction> {
        return messages.mapNotNull { (body, sender) -> parse(body, sender) }
    }

    /**
     * Check if this SMS is a bank transaction message.
     */
    fun isTransactionSms(body: String): Boolean {
        val lower = body.lowercase()
        return (DEBIT_KEYWORDS.any { lower.contains(it) } ||
                CREDIT_KEYWORDS.any { lower.contains(it) }) &&
                AMOUNT_REGEX.containsMatchIn(body)
    }

    /**
     * Extract the transaction amount from SMS body.
     */
    fun extractAmount(body: String): Double? {
        return AMOUNT_REGEX.find(body)?.groupValues?.get(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()
    }

    /**
     * Extract card last 4 digits.
     */
    fun extractCardLastFour(body: String): String? {
        return CARD_REGEX.find(body)?.groupValues?.get(1)
    }

    /**
     * Extract merchant name from SMS body.
     */
    fun extractMerchant(body: String): String? {
        return MERCHANT_REGEX.find(body)?.groupValues?.get(1)
            ?.trim()
            ?.take(50) // Cap merchant name length
    }

    /**
     * Determine if this is a debit (spend) transaction.
     */
    fun isDebitTransaction(body: String): Boolean {
        val lower = body.lowercase()
        return DEBIT_KEYWORDS.any { lower.contains(it) }
    }

    /**
     * Identify bank from sender ID.
     */
    fun identifyBank(sender: String): String? {
        val upper = sender.uppercase()
        return BANK_SENDERS.entries.find { upper.contains(it.key) }?.value
    }
}
