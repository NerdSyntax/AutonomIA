package com.nerdsyntax.juntalucas.feature.auth.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.auth.ui.common.isValidEmail
import com.nerdsyntax.juntalucas.feature.auth.ui.common.toAuthUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }

    fun register(nombreCompleto: String) {
        val state = _uiState.value
        val email = state.email.trim()
        val nombre = nombreCompleto.trim()

        val validationError = when {
            nombre.isBlank() -> "Ingresa tu nombre completo."
            !isValidEmail(email) -> "Ingresa un correo electrónico válido."
            state.password.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            state.password != state.confirmPassword -> "Las contraseñas no coinciden."
            else -> null
        }

        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.register(email, state.password)
                .onSuccess {
                    val uid = authRepository.currentUser.value?.uid
                    if (uid != null) {
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .set(mapOf("name" to nombre))
                    }

                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toAuthUserMessage()) }
                }
        }
    }
}