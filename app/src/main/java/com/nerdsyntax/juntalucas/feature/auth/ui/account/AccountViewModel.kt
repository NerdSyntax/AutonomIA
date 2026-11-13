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

class AccountViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AccountUiState(currentUser = authRepository.currentUser.value)
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _uiState.update { state ->
                    if (state.currentUser?.uid == user?.uid) {
                        state.copy(currentUser = user)
                    } else {
                        AccountUiState(currentUser = user)
                    }
                }
            }
        }
    }

    fun sendPasswordReset() {
        if (_uiState.value.isLoading) return

        val user = authRepository.currentUser.value
        val email = user?.email?.trim().orEmpty()

        if (user == null || email.isBlank()) {
            _uiState.update {
                it.copy(
                    successMessage = null,
                    errorMessage = "Tu cuenta no tiene un correo asociado."
                )
            }
            return
        }

        if (_uiState.value.currentUser?.uid != user.uid) return

        setLoading()

        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)

            if (authRepository.currentUser.value?.uid != user.uid) {
                return@launch
            }

            _uiState.update { state ->
                result.fold(
                    onSuccess = {
                        state.copy(
                            isLoading = false,
                            successMessage = "Te enviamos un enlace. Revisa tu correo y la carpeta de spam."
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isLoading = false,
                            errorMessage = error.toAuthUserMessage()
                        )
                    }
                )
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            setLoading()
            authRepository.deleteCurrentUser()
                .onSuccess {
                    logout()
                    _uiState.update { it.copy(isLoading = false) }
                }
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