package com.nerdsyntax.juntalucas.feature.ai.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AiUiState())
    val uiState = _uiState.asStateFlow()

    init {

    }

    fun onPeriodoSelected(periodo: String) {
        _uiState.update { it.copy(periodoSeleccionado = periodo) }
    }

    fun onAnalizarClick() {
        if (!_uiState.value.tieneDatosSuficientes) {
            _uiState.update { it.copy(mostrarAdvertencia = true) }
        } else {

        }
    }

    fun onOcultarAdvertencia() {
        _uiState.update { it.copy(mostrarAdvertencia = false) }
    }
}