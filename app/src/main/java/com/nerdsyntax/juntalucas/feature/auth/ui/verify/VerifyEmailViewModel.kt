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
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isVerificationConfirmed = it.isVerificationConfirmed &&
                            user?.isEmailVerified == true && user.uid == it.currentUser?.uid
                    )
                }
            }
        }
    }

    fun checkVerification() {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(isLoading = true, isVerificationConfirmed = false, successMessage = null, errorMessage = null)
        }
        viewModelScope.launch {
            authRepository.reloadCurrentUser()
                .onSuccess { user ->
                    val sameUser = user != null && user.uid == authRepository.currentUser.value?.uid
                    val verified = sameUser && user?.isEmailVerified == true
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isVerificationConfirmed = verified,
                            errorMessage = when {
                                !sameUser -> "Tu sesión terminó. Vuelve a iniciar sesión."
                                !verified -> "Tu correo aún no ha sido verificado. Revisa tu bandeja de entrada e inténtalo nuevamente."
                                else -> null
                            }
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No pudimos comprobar tu correo. Revisa tu conexión e inténtalo nuevamente."
                        )
                    }
                }
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
