package com.nerdsyntax.juntalucas.feature.dashboard.ui

data class DashboardUiState(
    val userEmail: String = "",
    val nombreNegocio: String = "",
    val metaMensual: Long = 0,
    val ventasTotales: Long = 0,
    val gastosTotales: Long = 0,
    val ganancia: Long = 0,
    val margen: String = "—",
    val movimientosRecientes: List<MovimientoUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class MovimientoUi(
    val id: String,
    val titulo: String,
    val fecha: String,
    val montoStr: String,
    val esIngreso: Boolean
)
