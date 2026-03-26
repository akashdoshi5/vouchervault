package com.addmrp.vault.domain.model

/**
 * Categories for voucher classification.
 */
enum class VoucherCategory(val displayName: String) {
    FOOD("Food"),
    FASHION("Fashion"),
    TRAVEL("Travel"),
    ELECTRONICS("Electronics"),
    ENTERTAINMENT("Entertainment"),
    GROCERY("Grocery"),
    HEALTH("Health"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): VoucherCategory =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}
