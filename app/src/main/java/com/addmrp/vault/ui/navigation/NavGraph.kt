package com.addmrp.vault.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.addmrp.vault.ui.audit.SpendAuditScreen
import com.addmrp.vault.ui.concierge.ConciergeScreen
import com.addmrp.vault.ui.creditcard.CreditCardScreen
import com.addmrp.vault.ui.rewards.RewardDashboardScreen
import com.addmrp.vault.ui.scan.ScanScreen
import com.addmrp.vault.ui.settings.SettingsScreen
import com.addmrp.vault.ui.sharing.VaultSharingScreen
import com.addmrp.vault.ui.swiper.OptimalSwiperScreen
import com.addmrp.vault.ui.wallet.WalletScreen

import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.addmrp.vault.ui.sharing.SharingViewModel
import com.addmrp.vault.ui.sharing.IngestionBottomSheet
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

@Composable
fun VaultNavGraph(
    navController: NavHostController,
    sharedImageUri: Uri? = null,
    sharedText: String? = null,
    deepLinkRewardId: String? = null
) {
    val sharingViewModel = hiltViewModel<SharingViewModel>()
    val walletViewModel = hiltViewModel<WalletViewModel>()
    val sharingState by sharingViewModel.sharingState.collectAsState()

    // Check if we have incoming deep links and we haven't already opened the scanner
    LaunchedEffect(deepLinkRewardId) {
        if (deepLinkRewardId != null) {
            // Find the voucher and start redemption
            val voucher = walletViewModel.uiState.value.vouchers.find { it.id == deepLinkRewardId }
            if (voucher != null) {
                walletViewModel.startRedemption(voucher)
            }
        }
    }

    // ── Global intent handling ──
    LaunchedEffect(sharedImageUri) {
        if (sharedImageUri != null) {
            sharingViewModel.onSharedImageReceived(sharedImageUri)
        }
    }

    LaunchedEffect(sharedText) {
        if (sharedText != null) {
            sharingViewModel.onSharedTextReceived(sharedText)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Wallet.route
        ) {
            composable(Screen.Wallet.route) {
                WalletScreen(
                    onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                    viewModel = walletViewModel
                )
            }
            composable(Screen.Scan.route) {
                // Now ScanScreen handles normal scanning + pre-filled data passed explicitly if needed.
                // For simplicity, we can still pass sharedImageUri if we want, but the BottomSheet 
                // intercepts it. For MVP, we'll keep it as is, or we could pass the OcrResult.
                // We'll leave sharedImageUri so existing behavior works if someone navigates directly.
                ScanScreen(sharedImageUri = sharedImageUri)
            }
            composable(Screen.Concierge.route) {
                ConciergeScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // ── MVP2 Screens ──
            composable(Screen.CreditCards.route) {
                CreditCardScreen()
            }
            composable(Screen.OptimalSwiper.route) {
                OptimalSwiperScreen()
            }
            composable(Screen.SpendAudit.route) {
                SpendAuditScreen()
            }

            // ── MVP2.5 Screens ──
            composable(Screen.Sharing.route) {
                VaultSharingScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Rewards.route) {
                RewardDashboardScreen()
            }
        }

        // Global Bottom Sheet for Zero-Interruption Ingestion
        IngestionBottomSheet(
            sharingState = sharingState,
            onDismissRequest = { sharingViewModel.dismiss() },
            onProceedToSave = { ocrResult ->
                sharingViewModel.onDataSavedOrDismissed()
                // Navigate to ScanScreen and inject prefilled data (we could pass it via Nav args or Shared ViewModel)
                // For now, let's navigate to Scan.
                navController.navigate(Screen.Scan.route) {
                    launchSingleTop = true
                }
            }
        )
    }
}
