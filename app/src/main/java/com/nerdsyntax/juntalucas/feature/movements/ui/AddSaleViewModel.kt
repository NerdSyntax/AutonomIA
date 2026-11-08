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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AddSaleViewModel(
    private val auth: AuthRepository,
    private val sales: SalesRepository,
    private val catalog: CatalogRepository,
    private val savedState: SavedStateHandle = SavedStateHandle(),
    private val initialSaleId: String? = null
) : ViewModel() {
    val isEditing: Boolean get() = initialSaleId != null

    private var owner: String? = savedState["owner"]
    private var requestId: String = savedState["requestId"] ?: UUID.randomUUID().toString()
    private var loadedSaleId: String? = savedState["loadedSaleId"]
    private var saveJob: Job? = null
    private val retry = MutableStateFlow(0)

    private val _editingSale = MutableStateFlow<Sale?>(null)
    val editingSale = _editingSale.asStateFlow()

    private val _uiState = MutableStateFlow(
        AddSaleUiState(
            date = savedState["date"] ?: SaleDates.today(),
            productId = savedState["productId"] ?: "",
            quantity = savedState["quantity"] ?: "1",
            unitPrice = savedState["unitPrice"] ?: "",
            discount = savedState["discount"] ?: "",
            note = savedState["note"] ?: "",
            paymentMethod = savedState.get<String>("payment")?.let(PaymentMethod::valueOf)
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(auth.currentUser.map { it?.uid }.distinctUntilChanged(), retry) { uid, _ -> uid }
                .collectLatest { uid ->
                    if (owner != uid) {
                        saveJob?.cancel()
                        owner = uid
                        requestId = UUID.randomUUID().toString()
                        loadedSaleId = null
                        _editingSale.value = null
                        _uiState.value = AddSaleUiState()
                        persist()
                    }

                    _uiState.update {
                        it.copy(products = emptyList(), isCatalogLoading = uid != null, catalogError = null)
                    }
                    if (uid == null) {
                        _uiState.update {
                            it.copy(catalogError = if (isEditing) {
                                "Inicia sesión para editar una venta."
                            } else {
                                "Inicia sesión para registrar una venta."
                            })
                        }
                        return@collectLatest
                    }

                    val saleId = initialSaleId
                    if (saleId != null) {
                        val result = sales.getById(uid, saleId)
                        if (auth.currentUser.value?.uid != uid || owner != uid) return@collectLatest
                        result.fold(
                            onSuccess = { sale ->
                                val draft = sale.draft
                                if (loadedSaleId != draft.id || _uiState.value.productId != draft.productId) {
                                    _uiState.update {
                                        it.copy(
                                            date = draft.saleDate,
                                            productId = draft.productId,
                                            quantity = draft.quantity.toString(),
                                            unitPrice = draft.unitPrice.toString(),
                                            discount = draft.discount.toString(),
                                            paymentMethod = draft.paymentMethod,
                                            note = draft.note
                                        )
                                    }
                                }
                                loadedSaleId = draft.id
                                _editingSale.value = sale
                                _uiState.update { it.copy(isCatalogLoading = false, catalogError = null) }
                                persist()
                            },
                            onFailure = { error ->
                                _editingSale.value = null
                                _uiState.update {
                                    it.copy(isCatalogLoading = false, catalogError = error.salesUserMessage())
                                }
                            }
                        )
                        return@collectLatest
                    }

                    catalog.observeActive(uid).collect { result ->
                        if (auth.currentUser.value?.uid != uid) return@collect
                        result.fold(
                            onSuccess = { data ->
                                _uiState.update {
                                    it.copy(
                                        products = data.value.sortedBy { p -> p.name.lowercase() },
                                        isCatalogLoading = false,
                                        catalogFromCache = data.fromCache,
                                        catalogError = if (data.fromCache && data.value.isEmpty()) {
                                            "No pudimos confirmar el catálogo. Revisa tu conexión y reintenta."
                                        } else null
                                    )
                                }
                            },
                            onFailure = { error ->
                                _uiState.update {
                                    it.copy(products = emptyList(), isCatalogLoading = false, catalogError = error.salesUserMessage())
                                }
                            }
                        )
                    }
                }
        }
    }

    private fun change(transform: (AddSaleUiState) -> AddSaleUiState) {
        val state = _uiState.value
        if (state.isSaving || state.isSuccess) return
        if (isEditing && (_editingSale.value == null || state.isCatalogLoading)) return
        _uiState.update { transform(it).copy(errorMessage = null) }
        persist()
    }

    fun onDateChange(value: String) = change { it.copy(date = value) }
    fun onQuantityChange(value: String) = change { it.copy(quantity = value) }
    fun onPriceChange(value: String) = change { it.copy(unitPrice = value) }
    fun onDiscountChange(value: String) = change { it.copy(discount = value) }
    fun onNoteChange(value: String) = change { it.copy(note = value) }
    fun onPaymentChange(value: PaymentMethod) = change { it.copy(paymentMethod = value) }

    fun onProductSelected(id: String) {
        if (isEditing) return
        change { state ->
            state.products.find { it.id == id }
                ?.let { state.copy(productId = id, unitPrice = it.price.toString()) }
                ?: state
        }
    }

    fun retryCatalog() {
        if (!_uiState.value.isSaving) retry.value += 1
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.isSuccess) return
        val original = _editingSale.value
        if (isEditing && (original == null || state.isCatalogLoading)) return

        val uid = auth.currentUser.value?.uid
        val draft = try {
            if (uid == null || uid != owner || (original != null && original.draft.userId != uid)) {
                throw DataValidationException("Tu sesión cambió. Vuelve a iniciar sesión.")
            }
            if (!isEditing && state.selectedProduct == null) {
                throw DataValidationException("Selecciona un producto o servicio activo.")
            }
            SaleDraft(
                id = original?.draft?.id ?: requestId,
                userId = uid,
                saleDate = state.date,
                productId = original?.draft?.productId ?: state.productId,
                quantity = parseQuantity(state.quantity)
                    ?: throw DataValidationException("Ingresa una cantidad entera positiva."),
                unitPrice = parseClp(state.unitPrice)
                    ?: throw DataValidationException("Ingresa un precio en pesos enteros."),
                discount = if (state.discount.isBlank()) 0L else parseClp(state.discount)
                    ?: throw DataValidationException("Ingresa un descuento válido en pesos CLP."),
                paymentMethod = state.paymentMethod
                    ?: throw DataValidationException("Selecciona un medio de pago."),
                note = state.note.trim()
            ).also { it.validate() }
        } catch (error: DataValidationException) {
            _uiState.update { it.copy(errorMessage = error.message) }
            return
        }

        persist()
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        saveJob = viewModelScope.launch {
            val result = if (isEditing) sales.update(draft) else sales.save(draft)
            if (auth.currentUser.value?.uid != draft.userId || owner != draft.userId) return@launch
            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, isSuccess = true) } },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.salesUserMessage()) }
                }
            )
        }
    }

    private fun persist() {
        val state = _uiState.value
        savedState["owner"] = owner
        savedState["requestId"] = requestId
        savedState["loadedSaleId"] = loadedSaleId
        savedState["date"] = state.date
        savedState["productId"] = state.productId
        savedState["quantity"] = state.quantity
        savedState["unitPrice"] = state.unitPrice
        savedState["discount"] = state.discount
        savedState["note"] = state.note
        savedState["payment"] = state.paymentMethod?.name
    }
}
