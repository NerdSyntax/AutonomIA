package com.nerdsyntax.juntalucas.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        cargarDatosDePrueba()
    }

    private fun cargarDatosDePrueba() {
        viewModelScope.launch {
            delay(800)

            _uiState.update {
                it.copy(
                    userName = "Camila",
                    businessName = "Pastelería Dulce Hogar",
                    metaVentasMensual = 2000000,
                    ventasAlcanzadas = 1800000,
                    ventasTotales = "$1.800.000",
                    gastosTotales = "$1.570.000",
                    ganancia = "$230.000",
                    margen = "12,8 %",
                    movimientosRecientes = listOf(
                        MovimientoUi("1", "Torta de cumpleaños", "04-09-2026 · Transferencia", "+$65.000", true),
                        MovimientoUi("2", "Compra de harina y azúcar", "04-09-2026 · Efectivo", "-$32.400", false),
                        MovimientoUi("3", "Cupcakes (6 docenas)", "03-09-2026 · Débito", "+$126.000", true),
                        MovimientoUi("4", "Gas butano", "03-09-2026 · Efectivo", "-$18.500", false)
                    ),
                    isLoading = false
                )
            }
        }
    }
}