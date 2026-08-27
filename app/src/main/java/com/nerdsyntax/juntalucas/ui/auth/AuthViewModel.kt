package com.nerdsyntax.juntalucas.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.nerdsyntax.juntalucas.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            currentUser = authRepository.getCurrentUser()
        )
    )

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    fun login(
        email: String,
        password: String
    ) {
        val cleanEmail = email.trim()

        if (!isValidEmail(cleanEmail)) {
            showError("Ingresa un correo electrónico válido.")
            return
        }

        if (password.isBlank()) {
            showError("Ingresa tu contraseña.")
            return
        }

        viewModelScope.launch {

            setLoading(true)

            authRepository
                .login(
                    email = cleanEmail,
                    password = password
                )
                .onSuccess { user ->

                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isLoading = false,
                            errorMessage = null,
                            infoMessage = null
                        )
                    }
                }
                .onFailure { error ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val cleanEmail = email.trim()

        if (!isValidEmail(cleanEmail)) {
            showError("Ingresa un correo electrónico válido.")
            return
        }

        if (password.length < 8) {
            showError(
                "La contraseña debe tener al menos 8 caracteres."
            )
            return
        }

        if (password != confirmPassword) {
            showError("Las contraseñas no coinciden.")
            return
        }

        viewModelScope.launch {

            setLoading(true)

            authRepository
                .register(
                    email = cleanEmail,
                    password = password
                )
                .onSuccess { user ->

                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isLoading = false,
                            errorMessage = null,
                            infoMessage =
                                "Cuenta creada. Revisa tu correo para verificarla."
                        )
                    }
                }
                .onFailure { error ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    fun sendPasswordReset(email: String) {
        val cleanEmail = email.trim()

        if (!isValidEmail(cleanEmail)) {
            showError("Ingresa un correo electrónico válido.")
            return
        }

        viewModelScope.launch {

            setLoading(true)

            authRepository
                .sendPasswordReset(cleanEmail)
                .onSuccess {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            infoMessage =
                                "Revisa tu correo para recuperar tu contraseña."
                        )
                    }
                }
                .onFailure { error ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    fun resendVerificationEmail() {

        viewModelScope.launch {

            setLoading(true)

            authRepository
                .sendEmailVerification()
                .onSuccess {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            infoMessage =
                                "Correo de verificación enviado."
                        )
                    }
                }
                .onFailure { error ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    fun reloadCurrentUser() {

        viewModelScope.launch {

            setLoading(true)

            authRepository
                .reloadCurrentUser()
                .onSuccess { user ->

                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    fun logout() {

        authRepository.logout()

        _uiState.value = AuthUiState()
    }

    fun deleteAccount() {

        viewModelScope.launch {

            setLoading(true)

            authRepository
                .deleteCurrentUser()
                .onSuccess {

                    _uiState.value = AuthUiState(
                        infoMessage = "Cuenta eliminada correctamente."
                    )
                }
                .onFailure { error ->

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    fun clearMessages() {

        _uiState.update {
            it.copy(
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    private fun setLoading(value: Boolean) {

        _uiState.update {
            it.copy(
                isLoading = value,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    private fun showError(message: String) {

        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = message,
                infoMessage = null
            )
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS
            .matcher(email)
            .matches()
    }

    private fun Throwable.toUserMessage(): String {

        return when (this) {

            is FirebaseAuthWeakPasswordException ->
                "La contraseña es demasiado débil."

            is FirebaseAuthUserCollisionException ->
                "Ya existe una cuenta registrada con este correo."

            is FirebaseAuthInvalidUserException ->
                "No existe una cuenta válida con este correo."

            is FirebaseAuthInvalidCredentialsException ->
                "Correo o contraseña incorrectos."

            is FirebaseTooManyRequestsException ->
                "Demasiados intentos. Intenta nuevamente más tarde."

            is FirebaseAuthRecentLoginRequiredException ->
                "Por seguridad debes volver a iniciar sesión antes de realizar esta acción."

            is FirebaseNetworkException ->
                "No se pudo conectar. Revisa tu conexión a Internet."

            else ->
                message ?: "Ocurrió un error inesperado."
        }
    }
}