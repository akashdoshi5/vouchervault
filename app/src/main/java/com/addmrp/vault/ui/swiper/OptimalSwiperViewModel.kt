package com.addmrp.vault.ui.swiper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.addmrp.vault.domain.model.AdvisorInsight
import com.addmrp.vault.domain.model.CreditCard
import com.addmrp.vault.domain.repository.CreditCardRepository
import com.addmrp.vault.domain.usecase.DebtDetectorUseCase
import com.addmrp.vault.domain.usecase.FrugalityFilter
import com.addmrp.vault.domain.usecase.OptimalSwiperUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * UI state for the "Which Card?" screen.
 * Rule 2: Handles Loading, Success, and Error states.
 */
data class SwiperUiState(
    val query: String = "",
    val amount: String = "1000",
    val cards: List<CreditCard> = emptyList(),
    val recommendation: AdvisorInsight? = null,
    val debtModeActive: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for the Optimal Swiper "Which Card?" screen.
 *
 * Rule 2: Single StateFlow output.
 * Rule 12: All insights pass through FrugalityFilter.
 */
@HiltViewModel
class OptimalSwiperViewModel @Inject constructor(
    private val creditCardRepository: CreditCardRepository,
    private val optimalSwiperUseCase: OptimalSwiperUseCase,
    private val debtDetectorUseCase: DebtDetectorUseCase,
    private val frugalityFilter: FrugalityFilter
) : ViewModel() {

    private val _userInput = MutableStateFlow(Pair("", "1000"))

    val uiState: StateFlow<SwiperUiState> = combine(
        _userInput,
        creditCardRepository.observeAllCards()
    ) { (query, amount), cards ->
        val debtActive = debtDetectorUseCase.isDebtModeActive(cards)

        val recommendation = if (query.isNotBlank()) {
            val rawInsight = optimalSwiperUseCase.execute(
                query = query,
                cards = cards,
                amount = amount.toDoubleOrNull() ?: 1000.0
            )
            // Rule 12: Filter through FrugalityFilter
            frugalityFilter.filter(rawInsight, debtActive)
        } else null

        SwiperUiState(
            query = query,
            amount = amount,
            cards = cards,
            recommendation = recommendation,
            debtModeActive = debtActive,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SwiperUiState()
    )

    fun onQueryChanged(query: String) {
        _userInput.update { it.copy(first = query) }
    }

    fun onAmountChanged(amount: String) {
        _userInput.update { it.copy(second = amount) }
    }
}
