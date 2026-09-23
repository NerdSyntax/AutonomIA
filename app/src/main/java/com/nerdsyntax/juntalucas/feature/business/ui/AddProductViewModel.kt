package com.nerdsyntax.juntalucas.feature.business.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AddProductViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState = _uiState.asStateFlow()

    fun onIsProductChange(isProduct: Boolean) = _uiState.update { it.copy(isProduct = isProduct) }
    fun onNombreChange(nombre: String) = _uiState.update { it.copy(nombre = nombre) }
    fun onCategoriaChange(categoria: String) = _uiState.update { it.copy(categoria = categoria) }
    fun onPrecioChange(precio: String) = _uiState.update { it.copy(precio = precio) }
    fun onCostoChange(costo: String) = _uiState.update { it.copy(costo = costo) }
    fun onDescripcionChange(descripcion: String) = _uiState.update { it.copy(descripcion = descripcion) }
    fun onStockActualChange(stock: String) = _uiState.update { it.copy(stockActual = stock) }
    fun onStockMinimoChange(stock: String) = _uiState.update { it.copy(stockMinimo = stock) }
    fun onUnidadChange(unidad: String) = _uiState.update { it.copy(unidad = unidad) }
    fun onIsActiveChange(isActive: Boolean) = _uiState.update { it.copy(isActive = isActive) }

    fun saveProduct(onSuccess: () -> Unit) {
        //aca teni que conectar la firebase
        onSuccess()
    }
}