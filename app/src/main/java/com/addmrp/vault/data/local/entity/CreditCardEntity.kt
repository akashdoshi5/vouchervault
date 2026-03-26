package com.addmrp.vault.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for credit cards.
 *
 * Rule 15: No full card numbers stored. Only last 4 digits.
 * Financial fields (creditLimit, currentBalance) should be
 * encrypted at rest via Room's SupportSQLiteOpenHelper.
 */
@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "card_name")
    val cardName: String,

    @ColumnInfo(name = "issuer")
    val issuer: String, // CardIssuer.name

    @ColumnInfo(name = "last_four_digits")
    val lastFourDigits: String,

    @ColumnInfo(name = "reward_type")
    val rewardType: String, // RewardType.name

    @ColumnInfo(name = "reward_rules_json")
    val rewardRulesJson: String, // JSON-serialized List<CardRewardRule>

    @ColumnInfo(name = "default_cashback_percent")
    val defaultCashbackPercent: Double,

    @ColumnInfo(name = "annual_fee")
    val annualFee: Double,

    @ColumnInfo(name = "annual_fee_waiver_spend")
    val annualFeeWaiverSpend: Double?,

    @ColumnInfo(name = "credit_limit")
    val creditLimit: Double?,

    @ColumnInfo(name = "current_balance")
    val currentBalance: Double?,

    @ColumnInfo(name = "is_revolving_credit")
    val isRevolvingCredit: Boolean,

    @ColumnInfo(name = "reward_points_balance")
    val rewardPointsBalance: Int,

    @ColumnInfo(name = "points_expiry_utc")
    val pointsExpiryUtc: Long?, // Epoch millis — Rule 4

    @ColumnInfo(name = "current_month_spend")
    val currentMonthSpend: Double,

    @ColumnInfo(name = "current_month_redeemed")
    val currentMonthRedeemed: Double,

    @ColumnInfo(name = "created_at_utc")
    val createdAtUtc: Long, // Epoch millis — Rule 4

    @ColumnInfo(name = "updated_at_utc")
    val updatedAtUtc: Long, // Epoch millis — Rule 4

    @ColumnInfo(name = "user_id")
    val userId: String
)
