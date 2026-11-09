package com.nerdsyntax.juntalucas.feature.paywall.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PaywallUiState(
    val planSeleccionado: String? = null,
    val mostrarMensajeProximamente: Boolean = false
)

class PaywallViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState = _uiState.asStateFlow()

    fun seleccionarPlan(planId: String) {
        if (planId == "FREE") return
        _uiState.update {
            it.copy(
                planSeleccionado = planId,
                mostrarMensajeProximamente = true
            )
        }
    }

    fun cerrarMensajeProximamente() {
        _uiState.update { it.copy(mostrarMensajeProximamente = false, planSeleccionado = null) }
    }
}