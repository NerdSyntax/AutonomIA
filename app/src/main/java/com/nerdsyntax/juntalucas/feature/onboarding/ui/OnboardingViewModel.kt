package com.nerdsyntax.juntalucas.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.feature.business.domain.Business
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessAuthenticationException
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

//base de datos con datos simulados locales para pruebas
object MockAppDatabase {
    var movements: List<DummyMovement> = emptyList()
}

data class DummyMovement(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Int,
    val type: String // "ingreso" o "gasto"
)

class OnboardingViewModel(private val businessRepository: BusinessRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun onNombreChange(nombre: String) = _uiState.update { it.copy(nombreNegocio = nombre, errorMessage = null) }
    fun onRubroChange(rubro: String) = _uiState.update { it.copy(rubro = rubro, errorMessage = null) }
    fun onRegionChange(region: String) = _uiState.update { it.copy(region = region) }
    fun onComunaChange(comuna: String) = _uiState.update { it.copy(comuna = comuna) }
    fun onMetaChange(meta: String) = _uiState.update { it.copy(metaMensual = meta) }

    fun onTipoActividadChange(tipo: String) = _uiState.update { it.copy(tipoActividad = tipo) }
    fun onPuntoPartidaChange(punto: String) = _uiState.update { it.copy(puntoPartida = punto) }

    fun validarPaso1(): Boolean {
        val state = _uiState.value
        if (state.nombreNegocio.isBlank() || state.rubro.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre y el rubro son obligatorios.") }
            return false
        }
        return true
    }

    fun finalizarConfiguracion() {
        if (_uiState.value.isLoading || _uiState.value.isSuccess || !validarPaso1()) return
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
        _uiState.update { it.copy(isLoading = true, isSuccess = false, errorMessage = null) }
        viewModelScope.launch {
            val result = businessRepository.saveBusiness(
                Business(
                    nombreNegocio = currentState.nombreNegocio.trim(),
                    rubro = currentState.rubro.trim(),
                    region = currentState.region.trim(),
                    comuna = currentState.comuna.trim(),
                    metaMensual = meta,
                    tipoActividad = currentState.tipoActividad,
                    puntoPartida = currentState.puntoPartida,
                    onboardingCompleted = true
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

            when (currentState.puntoPartida) {
                "manual" -> {
                    MockAppDatabase.movements = emptyList()
                }
                "ejemplo" -> {
                    MockAppDatabase.movements = listOf(
                        DummyMovement(description = "Venta pastel de chocolate", amount = 15000, type = "ingreso"),
                        DummyMovement(description = "Compra de harina y huevos", amount = 4500, type = "gasto"),
                        DummyMovement(description = "Venta 12 cupcakes surtidos", amount = 18000, type = "ingreso"),
                        DummyMovement(description = "Pago de electricidad", amount = 22000, type = "gasto"),
                        DummyMovement(description = "Venta torta de novios", amount = 65000, type = "ingreso")
                    )
                }
                "importar" -> {
                    MockAppDatabase.movements = emptyList()
                }
            }

            _uiState.update { it.copy(isLoading = false, isSuccess = true) }

        }
    }
}
