package com.addmrp.vault.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.addmrp.vault.domain.model.AdvisorInsight
import com.addmrp.vault.domain.model.CreditCard
import com.addmrp.vault.domain.model.Transaction
import com.addmrp.vault.domain.repository.CreditCardRepository
import com.addmrp.vault.domain.repository.TransactionRepository
import com.addmrp.vault.domain.usecase.DebtDetectorUseCase
import com.addmrp.vault.domain.usecase.FrugalityFilter
import com.addmrp.vault.domain.usecase.SpendAuditorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SpendAuditUiState(
    val transactions: List<Transaction> = emptyList(),
    val cards: List<CreditCard> = emptyList(),
    val insights: List<AdvisorInsight> = emptyList(),
    val totalMissedSavings: Double = 0.0,
    val debtModeActive: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class SpendAuditViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository,
    private val spendAuditorUseCase: SpendAuditorUseCase,
    private val debtDetectorUseCase: DebtDetectorUseCase,
    private val frugalityFilter: FrugalityFilter
) : ViewModel() {

    val uiState: StateFlow<SpendAuditUiState> = combine(
        transactionRepository.observeAllTransactions(),
        creditCardRepository.observeAllCards()
    ) { transactions, cards ->
        val debtActive = debtDetectorUseCase.isDebtModeActive(cards)
        val rawInsights = spendAuditorUseCase.execute(transactions, cards)
        val filteredInsights = frugalityFilter.filterAll(rawInsights, debtActive)
        val totalMissed = spendAuditorUseCase.calculateTotalMissedSavings(transactions, cards)

        SpendAuditUiState(
            transactions = transactions,
            cards = cards,
            insights = filteredInsights,
            totalMissedSavings = totalMissed,
            debtModeActive = debtActive,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SpendAuditUiState()
    )
}
