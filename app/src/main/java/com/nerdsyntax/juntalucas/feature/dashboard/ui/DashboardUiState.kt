package com.nerdsyntax.juntalucas.feature.dashboard.ui

data class DashboardUiState(
    val userName: String = "",
    val businessName: String = "",
    val metaVentasMensual: Int = 0,
    val ventasAlcanzadas: Int = 0,
    val ventasTotales: String = "",
    val gastosTotales: String = "",
    val ganancia: String = "",
    val margen: String = "",
    val movimientosRecientes: List<MovimientoUi> = emptyList(),
    val isLoading: Boolean = false
)

data class MovimientoUi(
    val id: String,
    val titulo: String,
    val fecha: String,
    val montoStr: String,
    val esIngreso: Boolean
)