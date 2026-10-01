package com.nerdsyntax.juntalucas.feature.profile.ui

import androidx.lifecycle.ViewModel
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {

    }

    fun onNotificacionesChange(enabled: Boolean) {
        _uiState.update { it.copy(notificacionesEnabled = enabled) }
    }

    fun onBiometriaChange(enabled: Boolean) {
        _uiState.update { it.copy(biometriaEnabled = enabled) }
    }

    fun logout() {

    }
}