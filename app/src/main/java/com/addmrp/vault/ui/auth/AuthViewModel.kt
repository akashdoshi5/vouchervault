package com.addmrp.vault.ui.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

enum class AuthMethod {
    EMAIL, GOOGLE
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginMode: Boolean = true,
    val currentMethod: AuthMethod = AuthMethod.EMAIL,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.update {
            when (field) {
                "email" -> it.copy(email = value, errorMessage = null)
                "password" -> it.copy(password = value, errorMessage = null)
                else -> it
            }
        }
    }

    fun setAuthMethod(method: AuthMethod) {
        _uiState.update { it.copy(currentMethod = method, errorMessage = null) }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode, errorMessage = null) }
    }

    fun setError(error: String) {
        _uiState.update { it.copy(errorMessage = error, isLoading = false) }
    }

    fun authenticateEmail(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            setError("Email and password cannot be empty")
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                if (state.isLoginMode) {
                    auth.signInWithEmailAndPassword(state.email.trim(), state.password).await()
                } else {
                    auth.createUserWithEmailAndPassword(state.email.trim(), state.password).await()
                }
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                setError(e.localizedMessage ?: "Authentication failed")
            }
        }
    }

    fun authenticateGoogle(idToken: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                setError(e.localizedMessage ?: "Google sign in failed")
            }
        }
    }
}
