package com.nerdsyntax.juntalucas.feature.auth.ui.login

import androidx.lifecycle.ViewModel
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.auth.ui.common.isValidEmail
import com.nerdsyntax.juntalucas.feature.auth.ui.common.toAuthUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, errorMessage = null) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, errorMessage = null) }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return
        val email = state.email.trim()
        val validationError = when {
            !isValidEmail(email) -> "Ingresa un correo electrónico válido."
            state.password.isBlank() -> "Ingresa tu contraseña."
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.login(email, state.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toAuthUserMessage()) }
                }
        }
    }
    // Called from the screen's coroutine scope: the Activity is never retained by this ViewModel.
    suspend fun loginWithGoogle(getIdToken: suspend () -> String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            authRepository.loginWithGoogle(getIdToken()).getOrThrow()
        } catch (_: GetCredentialCancellationException) {
            // Dismissing the account selector leaves the user on Login without an error.
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            val message = when (error) {
                is NoCredentialException -> "No encontramos una cuenta de Google disponible. Agrega una cuenta en tu dispositivo e inténtalo nuevamente."
                is FirebaseNetworkException -> "No se pudo conectar. Revisa tu conexión a Internet e inténtalo nuevamente."
                is FirebaseTooManyRequestsException -> "Demasiados intentos. Intenta nuevamente más tarde."
                is FirebaseAuthUserCollisionException -> "Este correo ya tiene una cuenta con otro método de acceso. Inicia sesión con ese método."
                else -> "No pudimos iniciar sesión con Google. Inténtalo nuevamente en unos momentos."
            }
            _uiState.update { it.copy(errorMessage = message) }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
