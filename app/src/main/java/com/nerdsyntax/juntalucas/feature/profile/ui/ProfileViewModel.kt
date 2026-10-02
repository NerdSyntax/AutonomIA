package com.nerdsyntax.juntalucas.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            launch {
                authRepository.currentUser.collectLatest { user ->
                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                name = user.displayName ?: "Usuario",
                                email = user.email ?: ""
                            )
                        }
                    }
                }
            }

            launch {
                businessRepository.getBusiness().onSuccess { business ->
                    if (business != null) {
                        val symbols = DecimalFormatSymbols().apply { groupingSeparator = '.' }
                        val formatter = DecimalFormat("#,###", symbols)
                        val metaFormateada = "$${formatter.format(business.metaMensual)}"

                        _uiState.update {
                            it.copy(
                                businessName = business.nombreNegocio,
                                rubro = business.rubro,
                                metaMensual = metaFormateada,
                                moneda = "Peso chileno (CLP)"
                            )
                        }
                    }
                }
            }
        }
    }

    fun onNotificacionesChange(enabled: Boolean) {
        _uiState.update { it.copy(notificacionesEnabled = enabled) }
    }

    fun onBiometriaChange(enabled: Boolean) {
        _uiState.update { it.copy(biometriaEnabled = enabled) }
    }

    fun logout() {
        authRepository.logout()
    }
}