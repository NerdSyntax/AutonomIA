package com.nerdsyntax.juntalucas.feature.movements.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.formatClp
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.Expense
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpensesRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.Sale
import com.nerdsyntax.juntalucas.feature.movements.domain.SaleDates
import com.nerdsyntax.juntalucas.feature.movements.domain.SalesRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.summarizeExpenses
import com.nerdsyntax.juntalucas.feature.movements.domain.summarizeSales
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpenseDeletionState(
    val expenseId: String? = null,
    val isDeleting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class SaleDeletionState(
    val saleId: String? = null,
    val isDeleting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class MovementsViewModel(
    private val auth: AuthRepository,
    private val repository: SalesRepository,
    private val expensesRepository: ExpensesRepository
) : ViewModel() {

    private val month = MutableStateFlow(SaleDates.today().take(7))
    private val retry = MutableStateFlow(0)

    private var sales: List<Sale> = emptyList()
    private var expenses: List<Expense> = emptyList()

    private var salesLoading = true
    private var expensesLoading = true
    private var salesError: String? = null
    private var expensesError: String? = null
    private var salesFromCache = false
    private var expensesFromCache = false

    private var activeUid: String? = null
    private var deleteJob: Job? = null
    private var saleDeleteJob: Job? = null

    private val _uiState = MutableStateFlow(
        MovementsUiState(month = month.value, isLoading = true)
    )
    val uiState = _uiState.asStateFlow()

    private val _expenseDeletion = MutableStateFlow(ExpenseDeletionState())
    val expenseDeletion = _expenseDeletion.asStateFlow()
    private val _saleDeletion = MutableStateFlow(SaleDeletionState())
    val saleDeletion = _saleDeletion.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                auth.currentUser.map { it?.uid }.distinctUntilChanged(),
                month,
                retry
            ) { uid, period, _ ->
                uid to period
            }.collectLatest { (uid, period) ->
                if (activeUid != uid) {
                    deleteJob?.cancel()
                    saleDeleteJob?.cancel()
                    _saleDeletion.value = SaleDeletionState()
                    _expenseDeletion.value = ExpenseDeletionState()
                    activeUid = uid
                }

                sales = emptyList()
                expenses = emptyList()
                salesLoading = uid != null
                expensesLoading = uid != null
                salesError = if (uid == null) "Inicia sesión para ver tus ventas." else null
                expensesError = if (uid == null) "Inicia sesión para ver tus gastos." else null
                salesFromCache = false
                expensesFromCache = false
                render()

                if (uid == null) return@collectLatest

                coroutineScope {
                    launch {
                        repository.observeMonth(uid, period).collect { result ->
                            if (auth.currentUser.value?.uid != uid || month.value != period) {
                                return@collect
                            }

                            salesLoading = false
                            result.fold(
                                onSuccess = { data ->
                                    sales = data.value
                                    salesFromCache = data.fromCache
                                    salesError = if (data.fromCache && data.value.isEmpty()) {
                                        "No pudimos confirmar las ventas del mes. Revisa tu conexión y reintenta."
                                    } else {
                                        null
                                    }
                                },
                                onFailure = { failure ->
                                    sales = emptyList()
                                    salesFromCache = false
                                    salesError = failure.salesUserMessage()
                                }
                            )
                            render()
                        }
                    }

                    launch {
                        expensesRepository.observeMonth(uid, period).collect { result ->
                            if (auth.currentUser.value?.uid != uid || month.value != period) {
                                return@collect
                            }

                            expensesLoading = false
                            result.fold(
                                onSuccess = { data ->
                                    expenses = data.value
                                    expensesFromCache = data.fromCache
                                    expensesError = if (data.fromCache && data.value.isEmpty()) {
                                        "No pudimos confirmar los gastos del mes. Revisa tu conexión y reintenta."
                                    } else {
                                        null
                                    }
                                },
                                onFailure = { failure ->
                                    expenses = emptyList()
                                    expensesFromCache = false
                                    expensesError = failure.salesUserMessage()
                                }
                            )
                            render()
                        }
                    }
                }
            }
        }
    }

    fun onTabSelected(tab: MovementTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        render()
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        render()
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        render()
    }

    fun retry() {
        retry.value += 1
    }

    fun changeMonth(offset: Int) {
        selectMonth(SaleDates.shiftMonth(month.value, offset))
    }

    fun selectMonth(value: String) {
        if (SaleDates.validMonth(value)) {
            month.value = value
            _uiState.update { it.copy(month = value) }
        }
    }

    fun onSaleSaved(period: String) {
        _uiState.update {
            it.copy(
                selectedTab = MovementTab.VENTAS,
                searchQuery = "",
                selectedFilter = "Todos"
            )
        }
        selectMonth(period)
        retry()
    }

    fun deleteExpense(expenseId: String) {
        if (_expenseDeletion.value.isDeleting) return

        val uid = auth.currentUser.value?.uid

        if (uid == null || uid != activeUid) {
            _expenseDeletion.value = ExpenseDeletionState(
                expenseId = expenseId,
                errorMessage = "Tu sesión cambió. Vuelve a iniciar sesión."
            )
            return
        }

        if (expenses.none { it.draft.id == expenseId && it.draft.userId == uid }) {
            _expenseDeletion.value = ExpenseDeletionState(
                expenseId = expenseId,
                errorMessage = "Este gasto ya no está disponible. Actualiza el listado."
            )
            return
        }

        _expenseDeletion.value = ExpenseDeletionState(
            expenseId = expenseId,
            isDeleting = true
        )

        deleteJob = viewModelScope.launch {
            val result = expensesRepository.delete(uid, expenseId)
            if (auth.currentUser.value?.uid != uid || activeUid != uid) return@launch

            _expenseDeletion.value = result.fold(
                onSuccess = {
                    ExpenseDeletionState(expenseId = expenseId, isSuccess = true)
                },
                onFailure = { error ->
                    ExpenseDeletionState(
                        expenseId = expenseId,
                        errorMessage = error.salesUserMessage()
                    )
                }
            )
        }
    }

    fun clearExpenseDeletion() {
        if (!_expenseDeletion.value.isDeleting) {
            _expenseDeletion.value = ExpenseDeletionState()
        }
    }

    fun deleteSale(saleId: String) {
        if (_saleDeletion.value.isDeleting) return
        val uid = auth.currentUser.value?.uid
        if (uid == null || uid != activeUid) {
            _saleDeletion.value = SaleDeletionState(
                saleId = saleId,
                errorMessage = "Tu sesión cambió. Vuelve a iniciar sesión."
            )
            return
        }
        if (sales.none { it.draft.id == saleId && it.draft.userId == uid }) {
            _saleDeletion.value = SaleDeletionState(
                saleId = saleId,
                errorMessage = "Esta venta ya no está disponible. Actualiza el listado."
            )
            return
        }

        _saleDeletion.value = SaleDeletionState(saleId = saleId, isDeleting = true)
        saleDeleteJob = viewModelScope.launch {
            val result = repository.delete(uid, saleId)
            if (auth.currentUser.value?.uid != uid || activeUid != uid) return@launch

            _saleDeletion.value = result.fold(
                onSuccess = { SaleDeletionState(saleId = saleId, isSuccess = true) },
                onFailure = { error ->
                    SaleDeletionState(saleId = saleId, errorMessage = error.salesUserMessage())
                }
            )
        }
    }

    fun clearSaleDeletion() {
        if (!_saleDeletion.value.isDeleting) _saleDeletion.value = SaleDeletionState()
    }

    private fun render() {
        val state = _uiState.value
        val query = state.searchQuery.trim()

        if (state.selectedTab == MovementTab.GASTOS) {
            val visible = expenses.filter { expense ->
                val draft = expense.draft
                val matchesFilter = state.selectedFilter == "Todos" ||
                        draft.paymentMethod.label == state.selectedFilter

                val matchesQuery = query.isBlank() ||
                        draft.description.contains(query, ignoreCase = true) ||
                        draft.category.contains(query, ignoreCase = true) ||
                        draft.note.contains(query, ignoreCase = true)

                matchesFilter && matchesQuery
            }.sortedWith(
                compareByDescending<Expense> { it.draft.expenseDate }
                    .thenByDescending { it.createdAtMillis }
                    .thenBy { it.draft.id }
            )

            val summary = runCatching { summarizeExpenses(visible) }
            val message = expensesError ?: if (summary.isFailure) {
                "El total del período supera el rango admitido."
            } else {
                null
            }

            _uiState.value = state.copy(
                month = month.value,
                isLoading = expensesLoading,
                errorMessage = message,
                fromCache = expensesFromCache,
                totalGastos = if (expensesLoading || message != null) {
                    "—"
                } else {
                    formatClp(summary.getOrThrow().total)
                },
                ticketPromedio = if (expensesLoading || message != null) {
                    "—"
                } else {
                    formatClp(summary.getOrThrow().average)
                },
                movements = if (message != null) {
                    emptyList()
                } else {
                    visible.map { expense ->
                        val draft = expense.draft
                        MovementItem(
                            draft.id,
                            draft.description,
                            SaleDates.display(draft.expenseDate),
                            draft.paymentMethod.label,
                            "-${formatClp(draft.amount)}",
                            false
                        )
                    }
                }
            )
            return
        }

        val visible = sales.filter { sale ->
            val matchesFilter = state.selectedFilter == "Todos" ||
                    sale.draft.paymentMethod.label == state.selectedFilter

            val matchesQuery = query.isBlank() ||
                    sale.productName.contains(query, ignoreCase = true) ||
                    sale.draft.note.contains(query, ignoreCase = true)

            matchesFilter && matchesQuery
        }.sortedWith(
            compareByDescending<Sale> { it.draft.saleDate }
                .thenByDescending { it.createdAtMillis }
                .thenBy { it.draft.id }
        )

        val summary = runCatching { summarizeSales(visible) }
        val message = salesError ?: if (summary.isFailure) {
            "El total del período supera el rango admitido."
        } else {
            null
        }

        _uiState.value = state.copy(
            month = month.value,
            isLoading = salesLoading,
            errorMessage = message,
            fromCache = salesFromCache,
            totalVendido = if (salesLoading || message != null) {
                "—"
            } else {
                formatClp(summary.getOrThrow().total)
            },
            ticketPromedio = if (salesLoading || message != null) {
                "—"
            } else {
                formatClp(summary.getOrThrow().average)
            },
            movements = if (message != null) {
                emptyList()
            } else {
                visible.map { sale ->
                    MovementItem(
                        sale.draft.id,
                        sale.productName,
                        SaleDates.display(sale.draft.saleDate),
                        sale.draft.paymentMethod.label,
                        "+${formatClp(sale.draft.total)}",
                        true
                    )
                }
            }
        )
    }
}
