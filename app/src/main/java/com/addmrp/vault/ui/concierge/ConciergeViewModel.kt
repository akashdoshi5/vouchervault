package com.addmrp.vault.ui.concierge

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ConciergeUiState(
    val searchQuery: String = "",
    val isGmailScrapingActive: Boolean = true,
    val isSmsListenerConnected: Boolean = true,
    // Mock data for MVP1
    val recommendedBrand: String = "Zomato",
    val recommendedOffer: String = "Flat ₹100 OFF",
    val recommendedReason: String = "\"Best value for your current cart. This coupon combines with your Pro membership for maximum savings.\"",
    val familyMembers: List<FamilyMember> = listOf(
        FamilyMember("Rahul", ""),
        FamilyMember("Priya", ""),
        FamilyMember("Arjun", "")
    ),
    val sharedVaultItems: List<SharedVaultItem> = listOf(
        SharedVaultItem("Amazon Pay - ₹200 Cashback", "Added by Priya", "2h ago"),
        SharedVaultItem("Starbucks - BOGO", "Added by Rahul", "5h ago")
    )
)

data class FamilyMember(val name: String, val avatarUrl: String)
data class SharedVaultItem(val title: String, val addedBy: String, val timeAgo: String)

@HiltViewModel
class ConciergeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ConciergeUiState())
    val uiState: StateFlow<ConciergeUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // MVP1: Mock response — in production, this would hit an AI service
    }
}
