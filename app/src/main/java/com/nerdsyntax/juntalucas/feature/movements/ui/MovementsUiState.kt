package com.nerdsyntax.juntalucas.feature.movements.ui
data class MovementsUiState(
    val selectedTab: MovementTab = MovementTab.VENTAS,
    val searchQuery: String = "",
    val selectedFilter: String = "Todos",
    val movements: List<MovementItem> = emptyList(),
    val totalVendido: String = "$0",
    val ticketPromedio: String = "$0",
    val month: String = com.nerdsyntax.juntalucas.feature.movements.domain.SaleDates.today().take(7),
    val errorMessage: String? = null,
    val fromCache: Boolean = false,
    val totalGastos: String = "$0",
    val isLoading: Boolean = false
)
enum class MovementTab { VENTAS, GASTOS }
data class MovementItem(
    val id: String, val title: String, val date: String,
    val paymentMethod: String, val amount: String, val isIncome: Boolean
)
