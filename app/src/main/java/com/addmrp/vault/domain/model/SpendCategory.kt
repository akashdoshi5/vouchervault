package com.addmrp.vault.domain.model

/**
 * Spend categories for Indian market credit card reward optimization.
 * Used by OptimalSwiperUseCase to match transactions to card reward rules.
 */
enum class SpendCategory(val displayName: String, val icon: String) {
    GROCERIES("Groceries", "🛒"),
    FUEL("Fuel", "⛽"),
    DINING("Dining", "🍽️"),
    TRAVEL("Travel", "✈️"),
    ONLINE_SHOPPING("Online Shopping", "🛍️"),
    UTILITIES("Utilities", "💡"),
    RENT("Rent", "🏠"),
    ENTERTAINMENT("Entertainment", "🎬"),
    HEALTH("Health & Pharmacy", "💊"),
    EDUCATION("Education", "📚"),
    INSURANCE("Insurance", "🛡️"),
    EMI("EMI Payments", "📅"),
    INTERNATIONAL("International", "🌍"),
    WALLET_RECHARGE("Wallet Recharge", "📱"),
    GOVERNMENT("Government / Tax", "🏛️"),
    OTHER("Other", "📋");

    companion object {
        /**
         * Fuzzy-match a merchant name or transaction description to a category.
         * Used by SmsTransactionParser and OptimalSwiperUseCase.
         */
        fun fromMerchant(merchant: String): SpendCategory {
            val lower = merchant.lowercase()
            return when {
                lower.containsAny("blinkit", "bigbasket", "zepto", "jiomart", "dmart", "grofers", "grocery") -> GROCERIES
                lower.containsAny("petrol", "diesel", "fuel", "hp ", "iocl", "bpcl", "shell") -> FUEL
                lower.containsAny("swiggy", "zomato", "dominos", "pizza", "kfc", "mcdonald", "starbucks", "restaurant", "cafe", "dining") -> DINING
                lower.containsAny("makemytrip", "goibibo", "yatra", "irctc", "airline", "flight", "hotel", "oyo", "booking") -> TRAVEL
                lower.containsAny("amazon", "flipkart", "myntra", "ajio", "tatacliq", "meesho", "nykaa", "snapdeal") -> ONLINE_SHOPPING
                lower.containsAny("electricity", "water", "gas bill", "broadband", "jio", "airtel", "vodafone", "postpaid") -> UTILITIES
                lower.containsAny("rent", "nobroker", "housing") -> RENT
                lower.containsAny("netflix", "hotstar", "prime video", "spotify", "bookmyshow", "pvr", "inox") -> ENTERTAINMENT
                lower.containsAny("pharmeasy", "1mg", "netmeds", "apollo", "hospital", "clinic", "doctor") -> HEALTH
                lower.containsAny("coursera", "udemy", "college", "school", "tuition", "university") -> EDUCATION
                lower.containsAny("insurance", "lic", "hdfc life", "icici pru") -> INSURANCE
                lower.containsAny("emi", "loan", "installment") -> EMI
                lower.containsAny("forex", "international", "usd", "eur", "gbp") -> INTERNATIONAL
                lower.containsAny("paytm", "phonepe", "gpay", "recharge", "wallet") -> WALLET_RECHARGE
                lower.containsAny("tax", "gst", "govt", "government", "challan") -> GOVERNMENT
                else -> OTHER
            }
        }

        private fun String.containsAny(vararg keywords: String): Boolean =
            keywords.any { this.contains(it) }
    }
}
