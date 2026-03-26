package com.addmrp.vault.data.mapper

import com.addmrp.vault.data.local.entity.CreditCardEntity
import com.addmrp.vault.data.local.entity.RewardPointEntity
import com.addmrp.vault.data.local.entity.TransactionEntity
import com.addmrp.vault.domain.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/**
 * Mapper for Credit Card domain ↔ entity ↔ Firestore conversions.
 *
 * Rule 14: All mapper directions must have round-trip tests.
 * Rule 8: Single source of truth — this is the ONLY place
 *          where CreditCard conversion logic lives.
 */
object CreditCardMapper {

    // ═══════════════════════════════════════════
    //  CreditCard: Domain ↔ Entity
    // ═══════════════════════════════════════════

    fun domainToEntity(card: CreditCard): CreditCardEntity = CreditCardEntity(
        id = card.id,
        cardName = card.cardName,
        issuer = card.issuer.name,
        lastFourDigits = card.lastFourDigits,
        rewardType = card.rewardType.name,
        rewardRulesJson = rewardRulesToJson(card.rewardRules),
        defaultCashbackPercent = card.defaultCashbackPercent,
        annualFee = card.annualFee,
        annualFeeWaiverSpend = card.annualFeeWaiverSpend,
        creditLimit = card.creditLimit,
        currentBalance = card.currentBalance,
        isRevolvingCredit = card.isRevolvingCredit,
        rewardPointsBalance = card.rewardPointsBalance,
        pointsExpiryUtc = card.pointsExpiryUtc?.toEpochMilli(),
        currentMonthSpend = card.currentMonthSpend,
        currentMonthRedeemed = card.currentMonthRedeemed,
        createdAtUtc = card.createdAtUtc.toEpochMilli(),
        updatedAtUtc = card.updatedAtUtc.toEpochMilli(),
        userId = card.userId
    )

    fun entityToDomain(entity: CreditCardEntity): CreditCard = CreditCard(
        id = entity.id,
        cardName = entity.cardName,
        issuer = runCatching { CardIssuer.valueOf(entity.issuer) }.getOrDefault(CardIssuer.OTHER),
        lastFourDigits = entity.lastFourDigits,
        rewardType = runCatching { RewardType.valueOf(entity.rewardType) }.getOrDefault(RewardType.CASHBACK),
        rewardRules = jsonToRewardRules(entity.rewardRulesJson),
        defaultCashbackPercent = entity.defaultCashbackPercent,
        annualFee = entity.annualFee,
        annualFeeWaiverSpend = entity.annualFeeWaiverSpend,
        creditLimit = entity.creditLimit,
        currentBalance = entity.currentBalance,
        isRevolvingCredit = entity.isRevolvingCredit,
        rewardPointsBalance = entity.rewardPointsBalance,
        pointsExpiryUtc = entity.pointsExpiryUtc?.let { Instant.ofEpochMilli(it) },
        currentMonthSpend = entity.currentMonthSpend,
        currentMonthRedeemed = entity.currentMonthRedeemed,
        createdAtUtc = Instant.ofEpochMilli(entity.createdAtUtc),
        updatedAtUtc = Instant.ofEpochMilli(entity.updatedAtUtc),
        userId = entity.userId
    )

    // ═══════════════════════════════════════════
    //  CreditCard: Domain ↔ Firestore Map
    // ═══════════════════════════════════════════

    @Suppress("UNCHECKED_CAST")
    fun firestoreMapToDomain(map: Map<String, Any?>): CreditCard = CreditCard(
        id = map["id"] as? String ?: "",
        cardName = map["cardName"] as? String ?: "",
        issuer = runCatching { CardIssuer.valueOf(map["issuer"] as? String ?: "") }.getOrDefault(CardIssuer.OTHER),
        lastFourDigits = map["lastFourDigits"] as? String ?: "",
        rewardType = runCatching { RewardType.valueOf(map["rewardType"] as? String ?: "") }.getOrDefault(RewardType.CASHBACK),
        defaultCashbackPercent = (map["defaultCashbackPercent"] as? Number)?.toDouble() ?: 0.0,
        annualFee = (map["annualFee"] as? Number)?.toDouble() ?: 0.0,
        creditLimit = (map["creditLimit"] as? Number)?.toDouble(),
        currentBalance = (map["currentBalance"] as? Number)?.toDouble(),
        isRevolvingCredit = map["isRevolvingCredit"] as? Boolean ?: false,
        rewardPointsBalance = (map["rewardPointsBalance"] as? Number)?.toInt() ?: 0,
        pointsExpiryUtc = (map["pointsExpiryUtc"] as? Number)?.toLong()?.let { Instant.ofEpochMilli(it) },
        currentMonthSpend = (map["currentMonthSpend"] as? Number)?.toDouble() ?: 0.0,
        currentMonthRedeemed = (map["currentMonthRedeemed"] as? Number)?.toDouble() ?: 0.0,
        createdAtUtc = (map["createdAtUtc"] as? Number)?.toLong()?.let { Instant.ofEpochMilli(it) } ?: Instant.now(),
        updatedAtUtc = (map["updatedAtUtc"] as? Number)?.toLong()?.let { Instant.ofEpochMilli(it) } ?: Instant.now(),
        userId = map["userId"] as? String ?: ""
    )

    fun domainToFirestoreMap(card: CreditCard): Map<String, Any?> = mapOf(
        "id" to card.id,
        "cardName" to card.cardName,
        "issuer" to card.issuer.name,
        "lastFourDigits" to card.lastFourDigits,
        "rewardType" to card.rewardType.name,
        "defaultCashbackPercent" to card.defaultCashbackPercent,
        "annualFee" to card.annualFee,
        "creditLimit" to card.creditLimit,
        "currentBalance" to card.currentBalance,
        "isRevolvingCredit" to card.isRevolvingCredit,
        "rewardPointsBalance" to card.rewardPointsBalance,
        "pointsExpiryUtc" to card.pointsExpiryUtc?.toEpochMilli(),
        "currentMonthSpend" to card.currentMonthSpend,
        "currentMonthRedeemed" to card.currentMonthRedeemed,
        "createdAtUtc" to card.createdAtUtc.toEpochMilli(),
        "updatedAtUtc" to card.updatedAtUtc.toEpochMilli(),
        "userId" to card.userId
    )

    // ═══════════════════════════════════════════
    //  Transaction: Domain ↔ Entity
    // ═══════════════════════════════════════════

    fun transactionDomainToEntity(txn: Transaction): TransactionEntity = TransactionEntity(
        id = txn.id,
        amount = txn.amount,
        merchant = txn.merchant,
        category = txn.category.name,
        cardId = txn.cardId,
        cardName = txn.cardName,
        isDebitCard = txn.isDebitCard,
        isInternational = txn.isInternational,
        source = txn.source.name,
        transactionDateUtc = txn.transactionDateUtc.toEpochMilli(),
        createdAtUtc = txn.createdAtUtc.toEpochMilli(),
        userId = txn.userId
    )

    fun transactionEntityToDomain(entity: TransactionEntity): Transaction = Transaction(
        id = entity.id,
        amount = entity.amount,
        merchant = entity.merchant,
        category = runCatching { SpendCategory.valueOf(entity.category) }.getOrDefault(SpendCategory.OTHER),
        cardId = entity.cardId,
        cardName = entity.cardName,
        isDebitCard = entity.isDebitCard,
        isInternational = entity.isInternational,
        source = runCatching { TransactionSource.valueOf(entity.source) }.getOrDefault(TransactionSource.MANUAL),
        transactionDateUtc = Instant.ofEpochMilli(entity.transactionDateUtc),
        createdAtUtc = Instant.ofEpochMilli(entity.createdAtUtc),
        userId = entity.userId
    )

    // ═══════════════════════════════════════════
    //  RewardPoint: Domain ↔ Entity
    // ═══════════════════════════════════════════

    fun rewardPointDomainToEntity(rp: RewardPoint): RewardPointEntity = RewardPointEntity(
        id = rp.id,
        cardId = rp.cardId,
        cardName = rp.cardName,
        issuer = rp.issuer.name,
        balance = rp.balance,
        valuationTiersJson = valuationTiersToJson(rp.valuationTiers),
        bestValueInRupees = rp.bestValueInRupees,
        expiryUtc = rp.expiryUtc?.toEpochMilli(),
        lastUpdatedUtc = rp.lastUpdatedUtc.toEpochMilli(),
        userId = rp.userId
    )

    fun rewardPointEntityToDomain(entity: RewardPointEntity): RewardPoint = RewardPoint(
        id = entity.id,
        cardId = entity.cardId,
        cardName = entity.cardName,
        issuer = runCatching { CardIssuer.valueOf(entity.issuer) }.getOrDefault(CardIssuer.OTHER),
        balance = entity.balance,
        valuationTiers = jsonToValuationTiers(entity.valuationTiersJson),
        bestValueInRupees = entity.bestValueInRupees,
        expiryUtc = entity.expiryUtc?.let { Instant.ofEpochMilli(it) },
        lastUpdatedUtc = Instant.ofEpochMilli(entity.lastUpdatedUtc),
        userId = entity.userId
    )

    // ═══════════════════════════════════════════
    //  JSON Serialization Helpers
    // ═══════════════════════════════════════════

    private fun rewardRulesToJson(rules: List<CardRewardRule>): String {
        val arr = JSONArray()
        rules.forEach { rule ->
            val obj = JSONObject().apply {
                put("category", rule.category.name)
                put("cashbackPercent", rule.cashbackPercent)
                put("pointsPerHundredRupees", rule.pointsPerHundredRupees)
                put("pointValueInRupees", rule.pointValueInRupees)
                put("monthlyCap", rule.monthlyCap ?: JSONObject.NULL)
                put("redemptionFee", rule.redemptionFee)
                put("minMonthlySpend", rule.minMonthlySpend ?: JSONObject.NULL)
                put("forexMarkupPercent", rule.forexMarkupPercent)
                put("isActive", rule.isActive)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun jsonToRewardRules(json: String): List<CardRewardRule> {
        if (json.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                CardRewardRule(
                    category = runCatching { SpendCategory.valueOf(obj.getString("category")) }.getOrDefault(SpendCategory.OTHER),
                    cashbackPercent = obj.optDouble("cashbackPercent", 0.0),
                    pointsPerHundredRupees = obj.optDouble("pointsPerHundredRupees", 0.0),
                    pointValueInRupees = obj.optDouble("pointValueInRupees", 0.0),
                    monthlyCap = if (obj.isNull("monthlyCap")) null else obj.optDouble("monthlyCap"),
                    redemptionFee = obj.optDouble("redemptionFee", 0.0),
                    minMonthlySpend = if (obj.isNull("minMonthlySpend")) null else obj.optDouble("minMonthlySpend"),
                    forexMarkupPercent = obj.optDouble("forexMarkupPercent", 0.0),
                    isActive = obj.optBoolean("isActive", true)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun valuationTiersToJson(tiers: List<ValuationTier>): String {
        val arr = JSONArray()
        tiers.forEach { tier ->
            val obj = JSONObject().apply {
                put("channelName", tier.channelName)
                put("valuePerPoint", tier.valuePerPoint)
                put("totalValue", tier.totalValue)
                put("fee", tier.fee)
                put("netValue", tier.netValue)
                put("isBestValue", tier.isBestValue)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun jsonToValuationTiers(json: String): List<ValuationTier> {
        if (json.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ValuationTier(
                    channelName = obj.optString("channelName", ""),
                    valuePerPoint = obj.optDouble("valuePerPoint", 0.0),
                    totalValue = obj.optDouble("totalValue", 0.0),
                    fee = obj.optDouble("fee", 0.0),
                    netValue = obj.optDouble("netValue", 0.0),
                    isBestValue = obj.optBoolean("isBestValue", false)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
