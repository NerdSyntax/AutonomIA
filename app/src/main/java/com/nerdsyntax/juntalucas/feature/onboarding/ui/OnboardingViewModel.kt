package com.nerdsyntax.juntalucas.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.Business
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessAuthenticationException
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun onNombreChange(nombre: String) = _uiState.update { it.copy(nombreNegocio = nombre, errorMessage = null) }
    fun onRubroChange(rubro: String) = _uiState.update { it.copy(rubro = rubro, errorMessage = null) }
    fun onRegionChange(region: String) = _uiState.update { it.copy(region = region) }
    fun onComunaChange(comuna: String) = _uiState.update { it.copy(comuna = comuna) }
    fun onMetaChange(meta: String) = _uiState.update { it.copy(metaMensual = meta) }

    fun onTipoActividadChange(tipo: String) = _uiState.update { it.copy(tipoActividad = tipo) }

    fun onPlanChange(plan: String) = _uiState.update { it.copy(planSuscripcion = plan) }

    fun validarPaso1(): Boolean {
        val state = _uiState.value
        if (state.nombreNegocio.isBlank() || state.rubro.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre y el rubro son obligatorios.") }
            return false
        }
        return true
    }

    fun finalizarConfiguracion(planElegido: String) {
        if (_uiState.value.isLoading || _uiState.value.isSuccess) return
        val currentState = _uiState.value

        if (currentState.tipoActividad !in setOf("productos", "servicios", "ambos")) {
            _uiState.update { it.copy(errorMessage = "Selecciona el tipo de actividad de tu negocio.") }
            return
        }
        val meta = currentState.metaMensual.trim().let { if (it.isEmpty()) 0L else it.toLongOrNull() }
        if (meta == null || meta < 0) {
            _uiState.update { it.copy(errorMessage = "Ingresa una meta mensual válida, sin puntos ni comas.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, isSuccess = false, errorMessage = null, planSuscripcion = planElegido) }

        viewModelScope.launch {
            val uid = authRepository.currentUser.value?.uid
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error: Sesión no encontrada.") }
                return@launch
            }
            val result = businessRepository.saveBusiness(
                Business(
                    nombreNegocio = currentState.nombreNegocio.trim(),
                    rubro = currentState.rubro.trim(),
                    region = currentState.region.trim(),
                    comuna = currentState.comuna.trim(),
                    metaMensual = meta,
                    tipoActividad = currentState.tipoActividad,
                    puntoPartida = "manual",
                    onboardingCompleted = true,
                    suscripcion = com.nerdsyntax.juntalucas.feature.business.domain.Suscripcion(plan = planElegido)
                )
            )

            if (result.isFailure) {
                val message = if (result.exceptionOrNull() is BusinessAuthenticationException) {
                    "Tu sesión cambió o terminó. Vuelve a iniciar sesión para guardar tu negocio."
                } else {
                    "No pudimos guardar tu negocio. Revisa tu conexión e inténtalo nuevamente."
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}