package com.nerdsyntax.juntalucas.feature.movements.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.parseClp
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AddExpenseViewModel(
    private val auth: AuthRepository,
    private val expenses: ExpensesRepository,
    private val savedState: SavedStateHandle = SavedStateHandle()
) : ViewModel() {
    private var owner: String? = savedState["owner"]
    private var requestId: String = savedState["requestId"] ?: UUID.randomUUID().toString()
    private val _uiState = MutableStateFlow(AddExpenseUiState(
        description = savedState["description"] ?: "", category = savedState["category"] ?: "",
        amount = savedState["amount"] ?: "", date = savedState["date"] ?: SaleDates.today(),
        type = savedState.get<String>("type")?.let { ExpenseType.valueOf(it) } ?: ExpenseType.VARIABLE,
        paymentMethod = savedState.get<String>("payment")?.let { PaymentMethod.valueOf(it) },
        note = savedState["note"] ?: ""
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            auth.currentUser.collect { user ->
                val uid = user?.uid
                if (owner != uid) {
                    owner = uid
                    requestId = UUID.randomUUID().toString()
                    if (uid != null) _uiState.value = AddExpenseUiState()
                    persist()
                }
            }
        }
    }

    private fun change(transform: (AddExpenseUiState) -> AddExpenseUiState) {
        if (_uiState.value.isSaving || _uiState.value.isSuccess) return
        _uiState.update { transform(it).copy(errorMessage = null) }
        persist()
    }
    fun onDescriptionChange(value: String) = change { it.copy(description = value) }
    fun onCategoryChange(value: String) = change { it.copy(category = value) }
    fun onAmountChange(value: String) = change { it.copy(amount = value) }
    fun onDateChange(value: String) = change { it.copy(date = value) }
    fun onTypeChange(value: ExpenseType) = change { it.copy(type = value) }
    fun onPaymentChange(value: PaymentMethod) = change { it.copy(paymentMethod = value) }
    fun onNoteChange(value: String) = change { it.copy(note = value) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.isSuccess) return
        val uid = auth.currentUser.value?.uid
        val draft = try {
            if (uid == null || uid != owner) throw DataValidationException("Tu sesión cambió. Vuelve a iniciar sesión.")
            ExpenseDraft(requestId, uid, state.description.trim(), state.category.trim(),
                parseClp(state.amount) ?: throw DataValidationException("Ingresa un monto válido en CLP."),
                state.date, state.type, state.paymentMethod ?: throw DataValidationException("Selecciona un medio de pago."), state.note.trim()
            ).also { it.validate() }
        } catch (error: DataValidationException) {
            _uiState.update { it.copy(errorMessage = error.message) }
            return
        }
        persist()
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            expenses.save(draft).fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, isSuccess = true) } },
                onFailure = { error -> _uiState.update { it.copy(isSaving = false, errorMessage = error.salesUserMessage()) } }
            )
        }
    }

    private fun persist() {
        val state = _uiState.value
        savedState["owner"] = owner; savedState["requestId"] = requestId
        savedState["description"] = state.description; savedState["category"] = state.category
        savedState["amount"] = state.amount; savedState["date"] = state.date
        savedState["type"] = state.type.name; savedState["payment"] = state.paymentMethod?.name
        savedState["note"] = state.note
    }
}
