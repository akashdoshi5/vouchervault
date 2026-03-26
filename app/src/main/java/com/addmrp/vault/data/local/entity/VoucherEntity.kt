package com.addmrp.vault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for local storage. All timestamps are stored as epoch millis in UTC
 * (Rule 4: Timezone & Expiry Rigidity).
 */
@Entity(tableName = "vouchers")
data class VoucherEntity(
    @PrimaryKey
    val id: String,
    val brand: String,
    val category: String,
    val code: String,
    val value: Double,
    val valueLabel: String,
    val source: String,
    val expiryUtcMillis: Long,
    val createdAtUtcMillis: Long,
    val updatedAtUtcMillis: Long,
    val isRedeemed: Boolean,
    val ownerId: String,
    val sharedWith: String, // Comma-separated UIDs for Room simplicity
    val addedBy: String,
    val brandLogoUrl: String,
    val notes: String,
    val inUseByUserId: String? = null,
    val inUseTimestampMillis: Long? = null,
    val lastUpdatedBy: String? = null
)
