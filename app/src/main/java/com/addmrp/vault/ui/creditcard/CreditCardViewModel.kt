package com.addmrp.vault.ui.creditcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.addmrp.vault.domain.model.CardIssuer
import com.addmrp.vault.domain.model.CreditCard
import com.addmrp.vault.domain.model.RewardType
import com.addmrp.vault.domain.repository.CreditCardRepository
import com.addmrp.vault.domain.usecase.DebtDetectorUseCase
import com.addmrp.vault.domain.usecase.DebtStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreditCardUiState(
    val cards: List<CreditCard> = emptyList(),
    val debtStatus: DebtStatus = DebtStatus.HEALTHY,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    // Add card form fields
    val showAddCardForm: Boolean = false,
    val formCardName: String = "",
    val formIssuer: CardIssuer = CardIssuer.HDFC,
    val formLastFour: String = "",
    val formRewardType: RewardType = RewardType.CASHBACK,
    val formDefaultCashback: String = "1.0",
    val formAnnualFee: String = "0",
    val showIssuerDropdown: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class CreditCardViewModel @Inject constructor(
    private val creditCardRepository: CreditCardRepository,
    private val debtDetectorUseCase: DebtDetectorUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(CreditCardUiState())

    val uiState: StateFlow<CreditCardUiState> = combine(
        _formState,
        creditCardRepository.observeAllCards()
    ) { form, cards ->
        form.copy(
            cards = cards,
            debtStatus = debtDetectorUseCase.execute(cards),
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CreditCardUiState()
    )

    fun toggleAddCardForm() {
        _formState.update { it.copy(showAddCardForm = !it.showAddCardForm) }
    }

    fun onCardNameChanged(name: String) {
        _formState.update { it.copy(formCardName = name) }
    }

    fun onIssuerSelected(issuer: CardIssuer) {
        _formState.update { it.copy(formIssuer = issuer, showIssuerDropdown = false) }
    }

    fun toggleIssuerDropdown() {
        _formState.update { it.copy(showIssuerDropdown = !it.showIssuerDropdown) }
    }

    fun onLastFourChanged(digits: String) {
        if (digits.length <= 4 && digits.all { it.isDigit() }) {
            _formState.update { it.copy(formLastFour = digits) }
        }
    }

    fun onDefaultCashbackChanged(value: String) {
        _formState.update { it.copy(formDefaultCashback = value) }
    }

    fun onAnnualFeeChanged(value: String) {
        _formState.update { it.copy(formAnnualFee = value) }
    }

    fun saveCard() {
        val state = _formState.value
        if (state.formCardName.isBlank()) {
            _formState.update { it.copy(error = "Card name is required") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, error = null) }
            try {
                val card = CreditCard(
                    cardName = state.formCardName,
                    issuer = state.formIssuer,
                    lastFourDigits = state.formLastFour,
                    rewardType = state.formRewardType,
                    defaultCashbackPercent = state.formDefaultCashback.toDoubleOrNull() ?: 1.0,
                    annualFee = state.formAnnualFee.toDoubleOrNull() ?: 0.0
                )
                creditCardRepository.addCard(card)
                _formState.update {
                    it.copy(
                        isSaving = false,
                        showAddCardForm = false,
                        saveSuccess = true,
                        formCardName = "",
                        formLastFour = "",
                        formDefaultCashback = "1.0",
                        formAnnualFee = "0"
                    )
                }
            } catch (e: Exception) {
                _formState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            try {
                creditCardRepository.deleteCard(cardId)
            } catch (e: Exception) {
                _formState.update { it.copy(error = e.message) }
            }
        }
    }

    fun resetSaveSuccess() {
        _formState.update { it.copy(saveSuccess = false) }
    }
}
