package com.nerdsyntax.juntalucas.feature.movements.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.parseClp
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.Expense
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpenseDraft
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpenseType
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpensesRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.PaymentMethod
import com.nerdsyntax.juntalucas.feature.movements.domain.SaleDates
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

data class ExpenseSuggestionsUiState(
    val items: List<ExpenseSuggestion> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AddExpenseViewModel(
    private val auth: AuthRepository,
    private val expenses: ExpensesRepository,
    private val savedState: SavedStateHandle = SavedStateHandle(),
    initialExpenseId: String? = null
) : ViewModel() {

    private val expenseId: String? =
        initialExpenseId ?: savedState.get<String>("expenseId")

    private var owner: String? = savedState["owner"]
    private var requestId: String =
        savedState["requestId"] ?: UUID.randomUUID().toString()

    private var loadedExpenseId: String? = savedState["loadedExpenseId"]
    private var loadJob: Job? = null
    private var saveJob: Job? = null
    private var suggestionsJob: Job? = null

    private val _isLoadingExpense = MutableStateFlow(false)
    val isLoadingExpense = _isLoadingExpense.asStateFlow()

    private val _suggestionsState = MutableStateFlow(ExpenseSuggestionsUiState())
    val suggestionsState = _suggestionsState.asStateFlow()

    val isEditing: Boolean get() = expenseId != null

    private val _uiState = MutableStateFlow(
        AddExpenseUiState(
            description = savedState["description"] ?: "",
            category = savedState["category"] ?: "",
            amount = savedState["amount"] ?: "",
            date = savedState["date"] ?: SaleDates.today(),
            type = savedState.get<String>("type")
                ?.let { ExpenseType.valueOf(it) } ?: ExpenseType.VARIABLE,
            paymentMethod = savedState.get<String>("payment")
                ?.let { PaymentMethod.valueOf(it) },
            note = savedState["note"] ?: ""
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        savedState["expenseId"] = expenseId

        viewModelScope.launch {
            auth.currentUser.collect { user ->
                val uid = user?.uid

                if (owner != uid) {
                    loadJob?.cancel()
                    saveJob?.cancel()
                    suggestionsJob?.cancel()
                    suggestionsJob = null
                    _isLoadingExpense.value = false
                    _suggestionsState.value = ExpenseSuggestionsUiState()

                    owner = uid
                    requestId = UUID.randomUUID().toString()
                    loadedExpenseId = null
                    _uiState.value = AddExpenseUiState()
                    persist()
                }

                if (uid != null) {
                    if (isEditing) {
                        loadExpenseIfNeeded(uid)
                    } else {
                        observeSuggestions(uid)
                    }
                }
            }
        }
    }

    private fun observeSuggestions(uid: String) {
        if (isEditing || suggestionsJob?.isActive == true) return

        _suggestionsState.update { it.copy(isLoading = true, errorMessage = null) }

        suggestionsJob = viewModelScope.launch {
            expenses.observeHistory(uid).collect { result ->
                if (owner != uid || auth.currentUser.value?.uid != uid) {
                    return@collect
                }

                result.fold(
                    onSuccess = { data ->
                        _suggestionsState.value = ExpenseSuggestionsUiState(
                            items = buildSuggestions(data.value)
                        )
                    },
                    onFailure = { error ->
                        _suggestionsState.update {
                            it.copy(isLoading = false, errorMessage = error.salesUserMessage())
                        }
                    }
                )
            }
        }
    }

    private fun buildSuggestions(history: List<Expense>): List<ExpenseSuggestion> {
        return history
            .sortedWith(
                compareByDescending<Expense> { it.draft.expenseDate }
                    .thenByDescending { it.createdAtMillis }
                    .thenBy { it.draft.id }
            )
            .distinctBy { expense ->
                expense.draft.description.trim().lowercase(Locale.ROOT) to
                        expense.draft.category.trim().lowercase(Locale.ROOT)
            }
            .map { expense ->
                ExpenseSuggestion(
                    description = expense.draft.description.trim(),
                    category = expense.draft.category.trim(),
                    amount = expense.draft.amount
                )
            }
    }

    fun retrySuggestions() {
        if (isEditing) return
        val uid = auth.currentUser.value?.uid ?: return
        if (uid != owner) return

        suggestionsJob?.cancel()
        suggestionsJob = null
        observeSuggestions(uid)
    }

    private fun loadExpenseIfNeeded(uid: String) {
        val id = expenseId ?: return
        if (loadedExpenseId == id || loadJob?.isActive == true) return

        _isLoadingExpense.value = true
        _uiState.update { it.copy(errorMessage = null) }

        loadJob = viewModelScope.launch {
            val result = expenses.getById(uid, id)

            if (owner != uid || auth.currentUser.value?.uid != uid) {
                return@launch
            }

            result.fold(
                onSuccess = { expense ->
                    val draft = expense.draft

                    _uiState.value = AddExpenseUiState(
                        description = draft.description,
                        category = draft.category,
                        amount = draft.amount.toString(),
                        date = draft.expenseDate,
                        type = draft.type,
                        paymentMethod = draft.paymentMethod,
                        note = draft.note
                    )

                    loadedExpenseId = id
                    persist()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.salesUserMessage())
                    }
                }
            )

            _isLoadingExpense.value = false
        }
    }

    fun retryLoad() {
        val uid = auth.currentUser.value?.uid ?: return
        if (uid == owner) {
            loadExpenseIfNeeded(uid)
        }
    }

    private fun change(transform: (AddExpenseUiState) -> AddExpenseUiState) {
        val state = _uiState.value

        if (state.isSaving || state.isSuccess || _isLoadingExpense.value) return
        if (expenseId != null && loadedExpenseId != expenseId) return

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

        if (state.isSaving || state.isSuccess || _isLoadingExpense.value) return
        if (expenseId != null && loadedExpenseId != expenseId) return

        val draft = try {
            val uid = auth.currentUser.value?.uid
            if (uid == null || uid != owner) {
                throw DataValidationException("Tu sesión cambió. Vuelve a iniciar sesión.")
            }

            ExpenseDraft(
                id = expenseId ?: requestId,
                userId = uid,
                description = state.description.trim(),
                category = state.category.trim(),
                amount = parseClp(state.amount)
                    ?: throw DataValidationException("Ingresa un monto válido en CLP."),
                expenseDate = state.date,
                type = state.type,
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
            val result = if (isEditing) {
                expenses.update(draft)
            } else {
                expenses.save(draft)
            }

            if (owner != draft.userId || auth.currentUser.value?.uid != draft.userId) {
                return@launch
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, isSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.salesUserMessage())
                    }
                }
            )
        }
    }

    private fun persist() {
        val state = _uiState.value

        savedState["owner"] = owner
        savedState["requestId"] = requestId
        savedState["loadedExpenseId"] = loadedExpenseId
        savedState["description"] = state.description
        savedState["category"] = state.category
        savedState["amount"] = state.amount
        savedState["date"] = state.date
        savedState["type"] = state.type.name
        savedState["payment"] = state.paymentMethod?.name
        savedState["note"] = state.note
    }
}