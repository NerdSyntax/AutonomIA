package com.nerdsyntax.juntalucas.feature.business.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.*
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogProduct
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogRepository
import com.nerdsyntax.juntalucas.feature.business.domain.validateProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AddProductViewModel(
    private val auth: AuthRepository,
    private val catalog: CatalogRepository,
    private val savedState: SavedStateHandle = SavedStateHandle(),
    private val initialProductId: String? = null
) : ViewModel() {
    private val owner: String? = savedState["owner"] ?: auth.currentUser.value?.uid
    private val requestId: String = savedState["requestId"] ?: UUID.randomUUID().toString()
    private val _uiState = MutableStateFlow(AddProductUiState(
        isProduct = savedState["isProduct"] ?: true, nombre = savedState["nombre"] ?: "",
        categoria = savedState["categoria"] ?: "", precio = savedState["precio"] ?: "",
        costo = savedState["costo"] ?: "", descripcion = savedState["descripcion"] ?: "",
        stockActual = savedState["stockActual"] ?: "0", stockMinimo = savedState["stockMinimo"] ?: "0",
        unidad = savedState["unidad"] ?: "unidad", isActive = savedState["isActive"] ?: true,
        trackStock = savedState["trackStock"] ?: true
    ))
    val uiState = _uiState.asStateFlow()
    init {
        persist()
        if (!initialProductId.isNullOrBlank()) {
            viewModelScope.launch {
                val uid = auth.currentUser.value?.uid ?: return@launch
                catalog.observeAll(uid).collect { result ->
                    result.getOrNull()?.value?.firstOrNull { it.id == initialProductId }?.let(::loadProduct)
                }
            }
        }
    }

    private fun loadProduct(product: CatalogProduct) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isProduct = product.isProduct, nombre = product.name, categoria = product.category,
            precio = product.price.toString(), costo = product.unitCost?.toString().orEmpty(),
            descripcion = product.description, stockActual = product.stock.toString(), stockMinimo = product.stockMinimum.toString(), unidad = product.unit,
            isActive = product.active, trackStock = product.trackStock) }
        persist()
    }

    private fun change(transform: (AddProductUiState) -> AddProductUiState) {
        if (_uiState.value.isSaving || _uiState.value.isSuccess) return
        _uiState.update { transform(it).copy(errorMessage = null) }
        persist()
    }
    fun onIsProductChange(value: Boolean) = change { it.copy(isProduct = value) }
    fun onNombreChange(value: String) = change { it.copy(nombre = value) }
    fun onCategoriaChange(value: String) = change { it.copy(categoria = value) }
    fun onPrecioChange(value: String) = change { it.copy(precio = value) }
    fun onCostoChange(value: String) = change { it.copy(costo = value) }
    fun onDescripcionChange(value: String) = change { it.copy(descripcion = value) }
    fun onStockActualChange(value: String) = change { it.copy(stockActual = value) }
    fun onStockMinimoChange(value: String) = change { it.copy(stockMinimo = value) }
    fun onUnidadChange(value: String) = change { it.copy(unidad = value) }
    fun onIsActiveChange(value: Boolean) = change { it.copy(isActive = value) }
    fun onTrackStockChange(value: Boolean) = change { it.copy(trackStock = value) }

    fun saveProduct() {
        val state = _uiState.value
        if (state.isSaving || state.isSuccess) return
        val product = try {
            val uid = owner?.takeIf { it == auth.currentUser.value?.uid }
                ?: throw DataValidationException("Tu sesión cambió. Vuelve a iniciar sesión.")
            fun stock(text: String): Long = text.trim().takeIf { it.matches(Regex("[0-9]+")) }
                ?.toLongOrNull()?.takeIf { it in 0..MAX_QUANTITY }
                ?: throw DataValidationException("El stock debe ser un entero entre 0 y 1.000.000.")
            CatalogProduct(initialProductId ?: requestId, uid, state.nombre.trim(), state.isProduct,
                parseClp(state.precio) ?: throw DataValidationException("Ingresa un precio válido en CLP."),
                if (state.costo.isBlank()) null else parseClp(state.costo)
                    ?: throw DataValidationException("Ingresa un costo válido o déjalo vacío si lo desconoces."),
                state.isActive, state.isProduct && state.trackStock,
                if (state.isProduct && state.trackStock) stock(state.stockActual) else 0,
                state.categoria.trim(), state.descripcion.trim(),
                if (state.isProduct && state.trackStock) stock(state.stockMinimo) else 0, state.unidad.trim()
            ).also(::validateProduct)
        } catch (error: DataValidationException) {
            _uiState.update { it.copy(errorMessage = error.message) }; return
        }
        persist()
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (initialProductId == null) catalog.create(product) else catalog.update(product)
            if (auth.currentUser.value?.uid != owner) return@launch
            result.fold(onSuccess = { _uiState.update { it.copy(isSaving = false, isSuccess = true) } },
                onFailure = { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.salesUserMessage()) } })
        }
    }

    private fun persist() {
        val state = _uiState.value
        savedState["owner"] = owner; savedState["requestId"] = requestId
        savedState["isProduct"] = state.isProduct; savedState["nombre"] = state.nombre
        savedState["categoria"] = state.categoria; savedState["precio"] = state.precio
        savedState["costo"] = state.costo; savedState["descripcion"] = state.descripcion
        savedState["stockActual"] = state.stockActual; savedState["stockMinimo"] = state.stockMinimo
        savedState["unidad"] = state.unidad; savedState["isActive"] = state.isActive
        savedState["trackStock"] = state.trackStock
    }
}
