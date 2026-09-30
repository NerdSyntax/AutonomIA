package com.nerdsyntax.juntalucas.feature.dashboard.ui

import com.nerdsyntax.juntalucas.feature.dashboard.domain.DashboardPeriodFilter

data class DashboardUiState(
    val userEmail: String = "",
    val nombreNegocio: String = "",
    val metaMensual: Long = 0,
    val ventasTotales: Long = 0,
    val ventasMesActual: Long = 0,
    val gastosTotales: Long = 0,
    val ganancia: Long = 0,
    val margen: String = "—",
    val balanceMovimientos: Long = 0,
    val selectedFilter: DashboardPeriodFilter = DashboardPeriodFilter.MONTH,
    val periodLabel: String = "Mes actual",
    val customStart: String = "",
    val customEnd: String = "",
    val fromCache: Boolean = false,
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
