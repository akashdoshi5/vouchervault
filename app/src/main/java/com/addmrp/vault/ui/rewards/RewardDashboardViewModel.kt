package com.addmrp.vault.ui.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.addmrp.vault.domain.model.CreditCard
import com.addmrp.vault.domain.model.Transaction
import com.addmrp.vault.domain.repository.CreditCardRepository
import com.addmrp.vault.domain.repository.TransactionRepository
import com.addmrp.vault.domain.usecase.DebtDetectorUseCase
import com.addmrp.vault.domain.usecase.RewardIntelligenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class RewardDashboardUiState(
    val monthlySummaries: List<RewardIntelligenceUseCase.MonthlyCardSummary> = emptyList(),
    val expiringRewards: List<RewardIntelligenceUseCase.ExpiringReward> = emptyList(),
    val cardSuggestions: List<RewardIntelligenceUseCase.CardSuggestion> = emptyList(),
    val totalPortfolioValue: Double = 0.0,
    val totalPointsBalance: Int = 0,
    val selectedCardBenefits: List<RewardIntelligenceUseCase.CardBenefit> = emptyList(),
    val selectedCardName: String? = null,
    val debtModeActive: Boolean = false,
    val cards: List<CreditCard> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RewardDashboardViewModel @Inject constructor(
    private val creditCardRepository: CreditCardRepository,
    private val transactionRepository: TransactionRepository,
    private val rewardIntelligence: RewardIntelligenceUseCase,
    private val debtDetector: DebtDetectorUseCase
) : ViewModel() {

    val uiState: StateFlow<RewardDashboardUiState> = combine(
        creditCardRepository.observeAllCards(),
        transactionRepository.observeAllTransactions()
    ) { cards, transactions ->
        val debtActive = debtDetector.isDebtModeActive(cards)
        val summaries = rewardIntelligence.getMonthlyRewardSummary(cards, transactions)
        val expiring = rewardIntelligence.getExpiringRewards(cards)
        val suggestions = if (debtActive) emptyList()
            else rewardIntelligence.suggestBetterCards(cards, transactions)

        val totalPoints = cards.sumOf { it.rewardPointsBalance }
        val totalValue = summaries.sumOf { it.pendingValueInRupees }

        RewardDashboardUiState(
            monthlySummaries = summaries,
            expiringRewards = expiring,
            cardSuggestions = suggestions,
            totalPortfolioValue = totalValue,
            totalPointsBalance = totalPoints,
            debtModeActive = debtActive,
            cards = cards,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        RewardDashboardUiState()
    )

    fun selectCardForBenefits(card: CreditCard) {
        // This is computed synchronously since it's a pure function
        val benefits = rewardIntelligence.getCardBenefits(card)
        // For simplicity we emit via a separate mechanism (could use MutableStateFlow)
        // In production, this would update a MutableStateFlow that combines with the main state
    }
}
