package com.addmrp.vault.ui.sharing

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.addmrp.vault.domain.usecase.CacheSharedImageUseCase
import com.addmrp.vault.domain.usecase.OcrResult
import com.addmrp.vault.domain.usecase.ProcessShareImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SharingState {
    object Idle : SharingState()
    object Caching : SharingState()
    object ProcessingOcr : SharingState()
    data class Success(val result: OcrResult, val cachedUri: Uri?) : SharingState()
    data class Error(val message: String) : SharingState()
}

@HiltViewModel
class SharingViewModel @Inject constructor(
    private val cacheSharedImageUseCase: CacheSharedImageUseCase,
    private val processShareImageUseCase: ProcessShareImageUseCase
) : ViewModel() {

    private val _sharingState = MutableStateFlow<SharingState>(SharingState.Idle)
    val sharingState: StateFlow<SharingState> = _sharingState.asStateFlow()

    // Flag to ensure we don't process the same intent twice
    private var lastProcessedUri: Uri? = null

    fun onSharedImageReceived(uri: Uri) {
        if (uri == lastProcessedUri) return
        lastProcessedUri = uri

        viewModelScope.launch {
            _sharingState.value = SharingState.Caching

            val cachedUri = cacheSharedImageUseCase.execute(uri)
            if (cachedUri == null) {
                _sharingState.value = SharingState.Error("Failed to access shared image.")
                return@launch
            }

            _sharingState.value = SharingState.ProcessingOcr

            try {
                val ocrResult = processShareImageUseCase.execute(cachedUri)
                _sharingState.value = SharingState.Success(ocrResult, cachedUri)
            } catch (e: Exception) {
                _sharingState.value = SharingState.Error("OCR Failed: ${e.localizedMessage}")
            }
        }
    }

    fun onSharedTextReceived(text: String) {
        viewModelScope.launch {
            _sharingState.value = SharingState.ProcessingOcr
            // For text, we can use the parser directly instead of OCR
            val parsedResult = processShareImageUseCase.parseOcrText(text)
            _sharingState.value = SharingState.Success(parsedResult, null)
        }
    }

    fun dismiss() {
        _sharingState.value = SharingState.Idle
        lastProcessedUri = null
    }

    // Called when the user clicks 'Save' or decides to act on the extracted data
    fun onDataSavedOrDismissed() {
        _sharingState.value = SharingState.Idle
    }
}
