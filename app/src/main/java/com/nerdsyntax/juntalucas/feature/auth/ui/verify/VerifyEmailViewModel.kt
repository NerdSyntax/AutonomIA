package com.nerdsyntax.juntalucas.feature.auth.ui.verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.auth.ui.common.toAuthUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerifyEmailViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(VerifyEmailUiState(authRepository.currentUser.value))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun checkVerification() = runOperation {
        authRepository.reloadCurrentUser().map { user ->
            if (user?.isEmailVerified == false) "El correo todavía no ha sido verificado." else null
        }
    }

    fun resendVerification() = runOperation {
        authRepository.sendEmailVerification().map { "Correo de verificación enviado." }
    }

    fun logout() = authRepository.logout()

    private fun runOperation(operation: suspend () -> Result<String?>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
            operation()
                .onSuccess { message ->
                    _uiState.update { it.copy(isLoading = false, successMessage = message) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toAuthUserMessage()) }
                }
        }
    }
}
