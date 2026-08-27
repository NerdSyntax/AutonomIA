package com.nerdsyntax.juntalucas.feature.auth.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.auth.ui.common.toAuthUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState(authRepository.currentUser.value))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.currentUser?.email?.takeIf(String::isNotBlank) ?: return
        viewModelScope.launch {
            setLoading()
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Revisa tu correo para cambiar tu contraseña.")
                    }
                }
                .onFailure(::showError)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            setLoading()
            authRepository.deleteCurrentUser()
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure(::showError)
        }
    }

    fun logout() = authRepository.logout()

    private fun setLoading() = _uiState.update {
        it.copy(isLoading = true, successMessage = null, errorMessage = null)
    }

    private fun showError(error: Throwable) = _uiState.update {
        it.copy(isLoading = false, errorMessage = error.toAuthUserMessage())
    }
}
