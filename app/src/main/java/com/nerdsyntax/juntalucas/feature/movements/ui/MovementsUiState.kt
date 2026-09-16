package com.nerdsyntax.juntalucas.feature.movements.ui
data class MovementsUiState(
    val selectedTab: MovementTab = MovementTab.VENTAS,
    val searchQuery: String = "",
    val selectedFilter: String = "Todos",
    val movements: List<MovementItem> = emptyList(),
    val totalVendido: String = "$1.800.000",
    val ticketPromedio: String = "$58.400",
    val totalGastos: String = "$1.570.000",
    val isLoading: Boolean = false
)
enum class MovementTab { VENTAS, GASTOS }
data class MovementItem(
    val id: String, val title: String, val date: String,
    val paymentMethod: String, val amount: String, val isIncome: Boolean
)