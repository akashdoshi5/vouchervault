package com.addmrp.vault.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Source of a transaction record.
 */
enum class TransactionSource(val displayName: String) {
    SMS("SMS Auto-detect"),
    MANUAL("Manual Entry"),
    STATEMENT("Statement Import"),
    OCR("Screenshot OCR")
}

/**
 * Domain model for a credit card transaction.
 *
 * Rule 4: All timestamps stored in UTC.
 * Rule 15: Transaction data processed on-device only.
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),

    /** Transaction amount in ₹ */
    val amount: Double,

    /** Merchant or payee name */
    val merchant: String,

    /** Auto-detected or user-selected spend category */
    val category: SpendCategory,

    /** Credit card ID used for this transaction */
    val cardId: String,

    /** Card name (denormalized for display) */
    val cardName: String = "",

    /** Whether this was a debit card transaction (affects audit advice) */
    val isDebitCard: Boolean = false,

    /** Whether this is an international transaction */
    val isInternational: Boolean = false,

    /** Source of this transaction record */
    val source: TransactionSource = TransactionSource.MANUAL,

    /** Transaction date in UTC — Rule 4 */
    val transactionDateUtc: Instant = Instant.now(),

    /** When this record was created in UTC — Rule 4 */
    val createdAtUtc: Instant = Instant.now(),

    /** Firestore user ID for cloud sync */
    val userId: String = ""
)
