package com.addmrp.vault.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.addmrp.vault.domain.model.Voucher
import com.addmrp.vault.domain.model.VoucherCategory
import com.addmrp.vault.domain.usecase.CalculateTotalAssetsUseCase
import com.addmrp.vault.domain.usecase.GetVouchersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import com.addmrp.vault.domain.repository.VoucherRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class WalletUiState(
    val vouchers: List<Voucher> = emptyList(),
    val totalAssets: Double = 0.0,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val redeemingVoucher: Voucher? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    getVouchersUseCase: GetVouchersUseCase,
    private val calculateTotalAssetsUseCase: CalculateTotalAssetsUseCase,
    private val voucherRepository: VoucherRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    private val _searchQuery = MutableStateFlow("")
    private val _redeemingVoucher = MutableStateFlow<Voucher?>(null)

    val uiState: StateFlow<WalletUiState> = combine(
        getVouchersUseCase(),
        _selectedCategory,
        _searchQuery,
        _redeemingVoucher
    ) { allVouchers, category, query, redeemingVoucher ->
        val filtered = allVouchers
            .filter { voucher ->
                (category == "All" || voucher.category.displayName.equals(category, ignoreCase = true))
            }
            .filter { voucher ->
                query.isBlank() || voucher.brand.contains(query, ignoreCase = true) ||
                        voucher.code.contains(query, ignoreCase = true) ||
                        voucher.notes.contains(query, ignoreCase = true)
            }
            .sortedBy { it.expiryUtc }

        // Update redeeming voucher if it exists in the stream (e.g. locked by someone else)
        val currentRedeeming = redeemingVoucher?.let { rv ->
            allVouchers.find { it.id == rv.id } ?: rv
        }

        WalletUiState(
            vouchers = filtered,
            totalAssets = calculateTotalAssetsUseCase(allVouchers),
            selectedCategory = category,
            searchQuery = query,
            isLoading = false,
            redeemingVoucher = currentRedeeming
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WalletUiState()
    )

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun startRedemption(voucher: Voucher) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            voucherRepository.acquireLock(voucher.id, uid)
            _redeemingVoucher.value = voucher
        }
    }

    fun cancelRedemption() {
        val voucher = _redeemingVoucher.value ?: return
        viewModelScope.launch {
            voucherRepository.releaseLock(voucher.id)
            _redeemingVoucher.value = null
        }
    }

    fun confirmRedemption() {
        val voucher = _redeemingVoucher.value ?: return
        viewModelScope.launch {
            voucherRepository.redeemVoucher(voucher.id)
            _redeemingVoucher.value = null
        }
    }
}
