package com.nerdsyntax.juntalucas.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

//base de datos con datos simulados locales para pruebas
object MockAppDatabase {
    var businessName: String = ""
    var businessCategory: String = ""
    var monthlyGoal: Int = 0
    var movements: List<DummyMovement> = emptyList()
}

data class DummyMovement(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Int,
    val type: String // "ingreso" o "gasto"
)

class OnboardingViewModel : ViewModel() {
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

    fun finalizarConfiguracion(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentState = _uiState.value

            MockAppDatabase.businessName = currentState.nombreNegocio
            MockAppDatabase.businessCategory = currentState.rubro
            MockAppDatabase.monthlyGoal = currentState.metaMensual.toIntOrNull() ?: 0

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

            delay(1500)

            _uiState.update { it.copy(isLoading = false) }

            onSuccess()
        }
    }
}