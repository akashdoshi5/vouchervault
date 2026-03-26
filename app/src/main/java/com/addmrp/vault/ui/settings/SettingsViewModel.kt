package com.addmrp.vault.ui.settings

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "Alexander Vault",
    val email: String = "alexander.v@blackcard.com",
    val memberBadge: String = "PRESTIGE MEMBER",
    val familyMembersCount: Int = 4,
    val isGmailScraperEnabled: Boolean = true,
    val isSmsListenerEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val isEncryptedVault: Boolean = true,
    val themeName: String = "Aurelian Reserve (Active)",
    val appVersion: String = "v1.0.0",
    val buildId: String = "001"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(
        userName = auth.currentUser?.displayName ?: "VoucherVault User",
        email = auth.currentUser?.email ?: "Not signed in"
    ))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleGmailScraper() {
        _uiState.update { it.copy(isGmailScraperEnabled = !it.isGmailScraperEnabled) }
    }

    fun toggleSmsListener() {
        _uiState.update { it.copy(isSmsListenerEnabled = !it.isSmsListenerEnabled) }
    }

    fun toggleBiometric() {
        _uiState.update { it.copy(isBiometricEnabled = !it.isBiometricEnabled) }
    }

    fun signOut() {
        auth.signOut()
    }
}
