package com.nerdsyntax.juntalucas.feature.movements.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.formatClp
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

class MovementsViewModel(private val auth: AuthRepository, private val repository: SalesRepository, private val expensesRepository: ExpensesRepository) : ViewModel() {
    private val month = MutableStateFlow(SaleDates.today().take(7))
    private val retry = MutableStateFlow(0)
    private var sales: List<Sale> = emptyList()
    private var expenses: List<Expense> = emptyList()
    private var loading = true
    private var error: String? = null
    private var fromCache = false
    private val _uiState = MutableStateFlow(MovementsUiState(month = month.value, isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(auth.currentUser.map { it?.uid }.distinctUntilChanged(), month, retry) { uid, period, _ -> uid to period }
                .collectLatest { (uid, period) ->
                    sales = emptyList(); expenses = emptyList(); loading = uid != null
                    error = if (uid == null) "Inicia sesión para ver tus ventas." else null
                    fromCache = false
                    render()
                    if (uid == null) return@collectLatest
                    coroutineScope {
                        launch { repository.observeMonth(uid, period).collect { result ->
                        if (auth.currentUser.value?.uid != uid || month.value != period) return@collect
                        loading = false
                        result.fold(onSuccess = { data ->
                            sales = data.value; fromCache = data.fromCache
                            error = if (data.fromCache && data.value.isEmpty()) "No pudimos confirmar las ventas del mes. Revisa tu conexión y reintenta." else null
                        }, onFailure = { failure -> sales = emptyList(); error = failure.salesUserMessage() })
                        render()
                        } }
                        launch {
                        expensesRepository.observeMonth(uid, period).collect { result ->
                            if (auth.currentUser.value?.uid != uid || month.value != period) return@collect
                            loading = false
                            result.fold(onSuccess = { data -> expenses = data.value; fromCache = fromCache || data.fromCache }, onFailure = { failure -> expenses = emptyList(); error = failure.salesUserMessage() })
                            render()
                        }
                        }
                    }
                }
        }
    }

    fun onTabSelected(tab: MovementTab) { _uiState.update { it.copy(selectedTab = tab) }; render() }
    fun onSearchChange(query: String) { _uiState.update { it.copy(searchQuery = query) }; render() }
    fun onFilterSelected(filter: String) { _uiState.update { it.copy(selectedFilter = filter) }; render() }
    fun retry() { retry.value += 1 }
    fun changeMonth(offset: Int) = selectMonth(SaleDates.shiftMonth(month.value, offset))
    fun selectMonth(value: String) {
        if (SaleDates.validMonth(value)) { month.value = value; _uiState.update { it.copy(month = value) } }
    }
    fun onSaleSaved(period: String) {
        _uiState.update { it.copy(selectedTab = MovementTab.VENTAS, searchQuery = "", selectedFilter = "Todos") }
        selectMonth(period)
        retry()
    }

    private fun render() {
        val state = _uiState.value
        if (state.selectedTab == MovementTab.GASTOS) {
            val visible = expenses.filter {
                (state.selectedFilter == "Todos" || it.draft.paymentMethod.label == state.selectedFilter) &&
                    (state.searchQuery.isBlank() || it.draft.description.contains(state.searchQuery.trim(), true) || it.draft.category.contains(state.searchQuery.trim(), true) || it.draft.note.contains(state.searchQuery.trim(), true))
            }.sortedWith(compareByDescending<Expense> { it.draft.expenseDate }.thenByDescending { it.createdAtMillis }.thenBy { it.draft.id })
            val summary = runCatching { summarizeExpenses(visible) }
            val message = error ?: if (summary.isFailure) "El total del período supera el rango admitido." else null
            _uiState.value = state.copy(month = month.value, isLoading = loading, errorMessage = message, fromCache = fromCache,
                totalGastos = if (loading || message != null) "—" else formatClp(summary.getOrThrow().total),
                ticketPromedio = if (loading || message != null) "—" else formatClp(summary.getOrThrow().average),
                movements = if (message != null) emptyList() else visible.map { MovementItem(it.draft.id, it.draft.description, SaleDates.display(it.draft.expenseDate), it.draft.paymentMethod.label, "-${formatClp(it.draft.amount)}", false) })
            return
        }
        val visible = sales.filter {
            (state.selectedFilter == "Todos" || it.draft.paymentMethod.label == state.selectedFilter) &&
                (state.searchQuery.isBlank() || it.productName.contains(state.searchQuery.trim(), ignoreCase = true) ||
                    it.draft.note.contains(state.searchQuery.trim(), ignoreCase = true))
        }.sortedWith(compareByDescending<Sale> { it.draft.saleDate }.thenByDescending { it.createdAtMillis }.thenBy { it.draft.id })
        val summary = runCatching { summarizeSales(visible) }
        val message = error ?: if (summary.isFailure) "El total del período supera el rango admitido." else null
        _uiState.value = state.copy(month = month.value, isLoading = loading, errorMessage = message, fromCache = fromCache,
            totalVendido = if (loading || message != null) "—" else formatClp(summary.getOrThrow().total),
            ticketPromedio = if (loading || message != null) "—" else formatClp(summary.getOrThrow().average),
            movements = if (message != null) emptyList() else visible.map {
                MovementItem(it.draft.id, it.productName, SaleDates.display(it.draft.saleDate),
                    it.draft.paymentMethod.label, "+${formatClp(it.draft.total)}", true)
            })
    }
}
