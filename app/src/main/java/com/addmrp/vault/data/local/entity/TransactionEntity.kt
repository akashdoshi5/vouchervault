package com.addmrp.vault.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for transactions.
 *
 * Rule 4: All timestamps in UTC epoch millis.
 * Rule 15: Processed on-device only; never sent raw to cloud.
 *
 * Indexed on cardId for fast joins and on transactionDateUtc for sorting.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["card_id"]),
        Index(value = ["transaction_date_utc"]),
        Index(value = ["category"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "merchant")
    val merchant: String,

    @ColumnInfo(name = "category")
    val category: String, // SpendCategory.name

    @ColumnInfo(name = "card_id")
    val cardId: String,

    @ColumnInfo(name = "card_name")
    val cardName: String,

    @ColumnInfo(name = "is_debit_card")
    val isDebitCard: Boolean,

    @ColumnInfo(name = "is_international")
    val isInternational: Boolean,

    @ColumnInfo(name = "source")
    val source: String, // TransactionSource.name

    @ColumnInfo(name = "transaction_date_utc")
    val transactionDateUtc: Long, // Epoch millis — Rule 4

    @ColumnInfo(name = "created_at_utc")
    val createdAtUtc: Long, // Epoch millis — Rule 4

    @ColumnInfo(name = "user_id")
    val userId: String
)
