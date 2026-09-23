package com.nerdsyntax.juntalucas.feature.business.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BusinessViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BusinessUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMockProducts()
    }

    fun onTabSelected(tab: BusinessTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    private fun loadMockProducts() {
        val list = listOf(
            ProductItem("1", "Torta personalizada", "Producto", "$120.000", "$68.000", "43%", "5 u."),
            ProductItem("2", "Cupcakes (docena)", "Producto", "$21.000", "$12.000", "43%", "24 u."),
            ProductItem("3", "Pie de limón (unidad)", "Producto", "$7.000", "$3.500", "50%", "8 u."),
            ProductItem("4", "Alfajores (caja 12)", "Producto", "$12.000", "$5.000", "58%", "15 u.")
        )
        _uiState.update { it.copy(products = list) }
    }
}