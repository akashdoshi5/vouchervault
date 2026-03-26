package com.addmrp.vault.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for reward point balances.
 *
 * Rule 4: Expiry in UTC epoch millis.
 * Rule 13: Valuations stored as JSON for multi-tier display.
 */
@Entity(
    tableName = "reward_points",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["card_id"])]
)
data class RewardPointEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "card_id")
    val cardId: String,

    @ColumnInfo(name = "card_name")
    val cardName: String,

    @ColumnInfo(name = "issuer")
    val issuer: String, // CardIssuer.name

    @ColumnInfo(name = "balance")
    val balance: Int,

    @ColumnInfo(name = "valuation_tiers_json")
    val valuationTiersJson: String, // JSON-serialized List<ValuationTier>

    @ColumnInfo(name = "best_value_in_rupees")
    val bestValueInRupees: Double,

    @ColumnInfo(name = "expiry_utc")
    val expiryUtc: Long?, // Epoch millis — Rule 4

    @ColumnInfo(name = "last_updated_utc")
    val lastUpdatedUtc: Long, // Epoch millis — Rule 4

    @ColumnInfo(name = "user_id")
    val userId: String
)
