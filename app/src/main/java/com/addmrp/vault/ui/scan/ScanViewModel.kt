package com.addmrp.vault.ui.scan

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.addmrp.vault.domain.model.RedemptionSource
import com.addmrp.vault.domain.model.Voucher
import com.addmrp.vault.domain.model.VoucherCategory
import com.addmrp.vault.domain.usecase.AddVoucherUseCase
import com.addmrp.vault.domain.usecase.ProcessShareImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

data class ScanUiState(
    val brand: String = "",
    val value: String = "",
    val code: String = "",
    val expiryDate: LocalDate? = null,
    val selectedCategory: VoucherCategory = VoucherCategory.FOOD,
    val selectedSource: RedemptionSource = RedemptionSource.GPAY,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val showDatePicker: Boolean = false,
    val categoryDropdownExpanded: Boolean = false,
    // ── Share-to-Vault OCR fields ──
    val isProcessingOcr: Boolean = false,
    val ocrRawText: String? = null,
    val sharedImageUri: Uri? = null,
    val isPrefilledFromOcr: Boolean = false
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val addVoucherUseCase: AddVoucherUseCase,
    private val processShareImageUseCase: ProcessShareImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    // ═══════════════════════════════════════════════════════
    // Share-to-Vault: Process a shared image through OCR
    // Called by MainActivity when an ACTION_SEND intent arrives
    // ═══════════════════════════════════════════════════════
    fun processSharedImage(imageUri: Uri) {
        _uiState.update {
            it.copy(
                sharedImageUri = imageUri,
                isProcessingOcr = true,
                error = null,
                isPrefilledFromOcr = false
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ocrResult = processShareImageUseCase.execute(imageUri)

                _uiState.update {
                    it.copy(
                        brand = ocrResult.brand ?: it.brand,
                        code = ocrResult.code ?: it.code,
                        value = ocrResult.value ?: it.value,
                        expiryDate = ocrResult.expiryDate ?: it.expiryDate,
                        ocrRawText = ocrResult.rawText,
                        isProcessingOcr = false,
                        isPrefilledFromOcr = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessingOcr = false,
                        error = "OCR failed: ${e.message ?: "Could not read image"}. Please enter details manually."
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // Form field handlers
    // ═══════════════════════════════════════════════════════
    fun onBrandChanged(brand: String) {
        _uiState.update { it.copy(brand = brand, error = null) }
    }

    fun onValueChanged(value: String) {
        _uiState.update { it.copy(value = value, error = null) }
    }

    fun onCodeChanged(code: String) {
        _uiState.update { it.copy(code = code, error = null) }
    }

    fun onExpiryDateSelected(date: LocalDate) {
        _uiState.update { it.copy(expiryDate = date, showDatePicker = false, error = null) }
    }

    fun onCategorySelected(category: VoucherCategory) {
        _uiState.update { it.copy(selectedCategory = category, categoryDropdownExpanded = false) }
    }

    fun onSourceSelected(source: RedemptionSource) {
        _uiState.update { it.copy(selectedSource = source, error = null) }
    }

    fun toggleDatePicker() {
        _uiState.update { it.copy(showDatePicker = !it.showDatePicker) }
    }

    fun toggleCategoryDropdown() {
        _uiState.update { it.copy(categoryDropdownExpanded = !it.categoryDropdownExpanded) }
    }

    // ═══════════════════════════════════════════════════════
    // Save to Vault
    // ═══════════════════════════════════════════════════════
    fun saveVoucher() {
        val current = _uiState.value

        // Validation
        if (current.brand.isBlank()) {
            _uiState.update { it.copy(error = "Brand name is required") }
            return
        }
        val numericValue = current.value.toDoubleOrNull()
        if (numericValue == null || numericValue <= 0) {
            _uiState.update { it.copy(error = "Enter a valid voucher value") }
            return
        }
        if (current.code.isBlank()) {
            _uiState.update { it.copy(error = "Voucher code is required") }
            return
        }
        if (current.expiryDate == null) {
            _uiState.update { it.copy(error = "Expiry date is required") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val voucher = Voucher(
                    brand = current.brand.trim(),
                    category = current.selectedCategory,
                    code = current.code.trim(),
                    value = numericValue,
                    valueLabel = "₹${numericValue.toLong()} OFF",
                    source = current.selectedSource,
                    expiryUtc = current.expiryDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                    createdAtUtc = Instant.now(),
                    updatedAtUtc = Instant.now()
                )
                addVoucherUseCase(voucher)
                _uiState.update {
                    ScanUiState(saveSuccess = true) // Reset form on success
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Save failed") }
            }
        }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
