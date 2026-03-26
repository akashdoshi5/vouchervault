package com.addmrp.vault.ui.navigation

/**
 * All screens in the VoucherVault app.
 * MVP2.5: Added Sharing, Rewards, CreditCards, OptimalSwiper, SpendAudit
 */
sealed class Screen(val route: String) {
    data object Wallet : Screen("wallet")
    data object Scan : Screen("scan")
    data object Concierge : Screen("concierge")
    data object Settings : Screen("settings")
    data object Auth : Screen("auth")

    // MVP2 screens
    data object CreditCards : Screen("credit_cards")
    data object OptimalSwiper : Screen("optimal_swiper")
    data object SpendAudit : Screen("spend_audit")

    // MVP2.5 screens
    data object Sharing : Screen("sharing")
    data object Rewards : Screen("rewards")
}
