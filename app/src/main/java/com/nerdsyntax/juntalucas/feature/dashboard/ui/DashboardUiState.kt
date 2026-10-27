package com.nerdsyntax.juntalucas.feature.dashboard.ui

import com.nerdsyntax.juntalucas.feature.dashboard.domain.DashboardPeriodFilter

data class DashboardUiState(
    val userName: String = "",
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
    val errorMessage: String? = null,
    val gananciaBruta: Double? = null,

    // Gráficos de ventas, gastos y ganancia bruta.
    val mesesGrafico: List<MesGraficoUi> = emptyList(),
    val graficosCargando: Boolean = true,
    val errorGraficos: String? = null,
    val graficosDesdeCache: Boolean = false,

    // Gastos agrupados por categoría del periodo seleccionado.
    val distribucionGastos: List<GastoCategoriaUi> = emptyList(),
    val ventasPorProducto: List<VentaProductoUi> = emptyList(),
)

data class MovimientoUi(
    val id: String,
    val titulo: String,
    val fecha: String,
    val montoStr: String,
    val esIngreso: Boolean
)

data class MesGraficoUi(
    val periodo: String,
    val etiqueta: String,
    val ventas: Long,
    val gastos: Long,
    val gananciaBruta: Double?,
    val tieneVentas: Boolean
)

data class GastoCategoriaUi(
    val categoria: String,
    val monto: Long
)

data class VentaProductoUi(
    val id: String,
    val nombre: String,
    val monto: Long
)