package com.addmrp.vault.data.mapper

import com.addmrp.vault.data.local.entity.VoucherEntity
import com.addmrp.vault.domain.model.RedemptionSource
import com.addmrp.vault.domain.model.Voucher
import com.addmrp.vault.domain.model.VoucherCategory
import java.time.Instant

/**
 * Maps between domain model, Room entity, and Firestore document.
 */
object VoucherMapper {

    fun entityToDomain(entity: VoucherEntity): Voucher = Voucher(
        id = entity.id,
        brand = entity.brand,
        category = VoucherCategory.fromString(entity.category),
        code = entity.code,
        value = entity.value,
        valueLabel = entity.valueLabel,
        source = RedemptionSource.fromString(entity.source),
        expiryUtc = Instant.ofEpochMilli(entity.expiryUtcMillis),
        createdAtUtc = Instant.ofEpochMilli(entity.createdAtUtcMillis),
        updatedAtUtc = Instant.ofEpochMilli(entity.updatedAtUtcMillis),
        isRedeemed = entity.isRedeemed,
        ownerId = entity.ownerId,
        sharedWith = entity.sharedWith.split(",").filter { it.isNotBlank() },
        addedBy = entity.addedBy,
        brandLogoUrl = entity.brandLogoUrl,
        notes = entity.notes,
        inUseByUserId = entity.inUseByUserId,
        inUseTimestampMillis = entity.inUseTimestampMillis,
        lastUpdatedBy = entity.lastUpdatedBy
    )

    fun domainToEntity(voucher: Voucher): VoucherEntity = VoucherEntity(
        id = voucher.id,
        brand = voucher.brand,
        category = voucher.category.name,
        code = voucher.code,
        value = voucher.value,
        valueLabel = voucher.valueLabel,
        source = voucher.source.name,
        expiryUtcMillis = voucher.expiryUtc.toEpochMilli(),
        createdAtUtcMillis = voucher.createdAtUtc.toEpochMilli(),
        updatedAtUtcMillis = voucher.updatedAtUtc.toEpochMilli(),
        isRedeemed = voucher.isRedeemed,
        ownerId = voucher.ownerId,
        sharedWith = voucher.sharedWith.joinToString(","),
        addedBy = voucher.addedBy,
        brandLogoUrl = voucher.brandLogoUrl,
        notes = voucher.notes,
        inUseByUserId = voucher.inUseByUserId,
        inUseTimestampMillis = voucher.inUseTimestampMillis,
        lastUpdatedBy = voucher.lastUpdatedBy
    )

    fun domainToFirestoreMap(voucher: Voucher): Map<String, Any?> = mapOf(
        "brand" to voucher.brand,
        "category" to voucher.category.name,
        "code" to voucher.code,
        "value" to voucher.value,
        "valueLabel" to voucher.valueLabel,
        "source" to voucher.source.name,
        "expiryUtcMillis" to voucher.expiryUtc.toEpochMilli(),
        "createdAtUtcMillis" to voucher.createdAtUtc.toEpochMilli(),
        "updatedAtUtcMillis" to voucher.updatedAtUtc.toEpochMilli(),
        "isRedeemed" to voucher.isRedeemed,
        "ownerId" to voucher.ownerId,
        "sharedWith" to voucher.sharedWith,
        "addedBy" to voucher.addedBy,
        "brandLogoUrl" to voucher.brandLogoUrl,
        "notes" to voucher.notes,
        "inUseByUserId" to voucher.inUseByUserId,
        "inUseTimestampMillis" to voucher.inUseTimestampMillis,
        "lastUpdatedBy" to voucher.lastUpdatedBy
    )

    @Suppress("UNCHECKED_CAST")
    fun firestoreMapToDomain(id: String, map: Map<String, Any?>): Voucher = Voucher(
        id = id,
        brand = map["brand"] as? String ?: "",
        category = VoucherCategory.fromString(map["category"] as? String ?: ""),
        code = map["code"] as? String ?: "",
        value = (map["value"] as? Number)?.toDouble() ?: 0.0,
        valueLabel = map["valueLabel"] as? String ?: "",
        source = RedemptionSource.fromString(map["source"] as? String ?: ""),
        expiryUtc = Instant.ofEpochMilli((map["expiryUtcMillis"] as? Number)?.toLong() ?: 0L),
        createdAtUtc = Instant.ofEpochMilli((map["createdAtUtcMillis"] as? Number)?.toLong() ?: 0L),
        updatedAtUtc = Instant.ofEpochMilli((map["updatedAtUtcMillis"] as? Number)?.toLong() ?: 0L),
        isRedeemed = map["isRedeemed"] as? Boolean ?: false,
        ownerId = map["ownerId"] as? String ?: "",
        sharedWith = (map["sharedWith"] as? List<String>) ?: emptyList(),
        addedBy = map["addedBy"] as? String ?: "",
        brandLogoUrl = map["brandLogoUrl"] as? String ?: "",
        notes = map["notes"] as? String ?: "",
        inUseByUserId = map["inUseByUserId"] as? String,
        inUseTimestampMillis = (map["inUseTimestampMillis"] as? Number)?.toLong(),
        lastUpdatedBy = map["lastUpdatedBy"] as? String
    )
}
