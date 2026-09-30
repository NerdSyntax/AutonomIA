package com.nerdsyntax.juntalucas.feature.movements.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.parseClp
import com.nerdsyntax.juntalucas.core.format.parseQuantity
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AddSaleViewModel(
    private val auth: AuthRepository,
    private val sales: SalesRepository,
    private val catalog: CatalogRepository,
    private val savedState: SavedStateHandle = SavedStateHandle()
) : ViewModel() {
    private var owner: String? = savedState["owner"]
    private var requestId: String = savedState["requestId"] ?: UUID.randomUUID().toString()
    private val retry = MutableStateFlow(0)
    private val _uiState = MutableStateFlow(AddSaleUiState(
        date = savedState["date"] ?: SaleDates.today(), productId = savedState["productId"] ?: "",
        quantity = savedState["quantity"] ?: "1", unitPrice = savedState["unitPrice"] ?: "",
        discount = savedState["discount"] ?: "", note = savedState["note"] ?: "",
        paymentMethod = savedState.get<String>("payment")?.let { PaymentMethod.valueOf(it) }
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(auth.currentUser.map { it?.uid }.distinctUntilChanged(), retry) { uid, _ -> uid }
                .collectLatest { uid ->
                    if (owner != uid) {
                        owner = uid
                        requestId = UUID.randomUUID().toString()
                        _uiState.value = AddSaleUiState()
                        persist()
                    }
                    _uiState.update { it.copy(products = emptyList(), isCatalogLoading = uid != null, catalogError = null) }
                    if (uid == null) {
                        _uiState.update { it.copy(catalogError = "Inicia sesión para registrar una venta.") }
                        return@collectLatest
                    }
                    catalog.observeActive(uid).collect { result ->
                        if (auth.currentUser.value?.uid != uid) return@collect
                        result.fold(onSuccess = { data ->
                            _uiState.update { it.copy(products = data.value.sortedBy { p -> p.name.lowercase() },
                                isCatalogLoading = false, catalogFromCache = data.fromCache,
                                catalogError = if (data.fromCache && data.value.isEmpty()) "No pudimos confirmar el catálogo. Revisa tu conexión y reintenta." else null) }
                        }, onFailure = { error ->
                            _uiState.update { it.copy(products = emptyList(), isCatalogLoading = false, catalogError = error.salesUserMessage()) }
                        })
                    }
                }
        }
    }

    private fun change(transform: (AddSaleUiState) -> AddSaleUiState) {
        if (_uiState.value.isSaving || _uiState.value.isSuccess) return
        _uiState.update { transform(it).copy(errorMessage = null) }
        persist()
    }
    fun onDateChange(value: String) = change { it.copy(date = value) }
    fun onQuantityChange(value: String) = change { it.copy(quantity = value) }
    fun onPriceChange(value: String) = change { it.copy(unitPrice = value) }
    fun onDiscountChange(value: String) = change { it.copy(discount = value) }
    fun onNoteChange(value: String) = change { it.copy(note = value) }
    fun onPaymentChange(value: PaymentMethod) = change { it.copy(paymentMethod = value) }
    fun onProductSelected(id: String) = change { state ->
        state.products.find { it.id == id }?.let { state.copy(productId = id, unitPrice = it.price.toString()) } ?: state
    }
    fun retryCatalog() { retry.value += 1 }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.isSuccess) return
        val uid = auth.currentUser.value?.uid
        val draft = try {
            if (uid == null || uid != owner) throw DataValidationException("Tu sesión cambió. Vuelve a iniciar sesión.")
            if (state.selectedProduct == null) throw DataValidationException("Selecciona un producto o servicio activo.")
            SaleDraft(requestId, uid, state.date, state.productId,
                parseQuantity(state.quantity) ?: throw DataValidationException("Ingresa una cantidad entera positiva."),
                parseClp(state.unitPrice) ?: throw DataValidationException("Ingresa un precio en pesos enteros."),
                if (state.discount.isBlank()) 0 else parseClp(state.discount) ?: throw DataValidationException("Ingresa un descuento válido en pesos CLP."),
                state.paymentMethod ?: throw DataValidationException("Selecciona un medio de pago."), state.note.trim()
            ).also { it.validate() }
        } catch (error: DataValidationException) {
            _uiState.update { it.copy(errorMessage = error.message) }; return
        }
        persist()
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = sales.save(draft)
            if (auth.currentUser.value?.uid != uid || owner != uid) return@launch
            result.fold(onSuccess = { _uiState.update { it.copy(isSaving = false, isSuccess = true) } },
                onFailure = { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.salesUserMessage()) } })
        }
    }

    private fun persist() {
        val state = _uiState.value
        savedState["owner"] = owner; savedState["requestId"] = requestId
        savedState["date"] = state.date; savedState["productId"] = state.productId
        savedState["quantity"] = state.quantity; savedState["unitPrice"] = state.unitPrice
        savedState["discount"] = state.discount; savedState["note"] = state.note
        savedState["payment"] = state.paymentMethod?.name
    }
}
