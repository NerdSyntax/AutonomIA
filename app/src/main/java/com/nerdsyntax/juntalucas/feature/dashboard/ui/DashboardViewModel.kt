package com.nerdsyntax.juntalucas.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.formatClp
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.Business
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import com.nerdsyntax.juntalucas.feature.dashboard.domain.*
import com.nerdsyntax.juntalucas.feature.movements.domain.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository,
    private val salesRepository: SalesRepository? = null,
    private val expensesRepository: ExpensesRepository? = null
) : ViewModel() {
    private val filter = MutableStateFlow(DashboardPeriodFilter.MONTH)
    private val customStart = MutableStateFlow("")
    private val customEnd = MutableStateFlow("")
    private val retry = MutableStateFlow(0)
    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private var business: Business? = null
    private var sales = emptyList<Sale>()
    private var monthlySales = emptyList<Sale>()
    private var expenses = emptyList<Expense>()
    private var error: String? = null
    private var loading = false
    private var fromCache = false
    private var pendingLoads = 0

    init {
        viewModelScope.launch {
            authRepository.currentUser.map { it?.uid }.distinctUntilChanged().collectLatest { uid ->
                business = null; sales = emptyList(); monthlySales = emptyList(); expenses = emptyList(); error = null; fromCache = false
                loading = uid != null
                _uiState.value = DashboardUiState(userEmail = authRepository.currentUser.value?.displayName?.takeIf { it.isNotBlank() } ?: authRepository.currentUser.value?.email.orEmpty(), isLoading = loading)
                if (uid == null) { _uiState.value = DashboardUiState(errorMessage = "Inicia sesión para ver tu negocio."); return@collectLatest }
                loadBusiness(uid)
                combine(filter, customStart, customEnd, retry) { selected, start, end, _ -> selected to (start to end) }
                    .collectLatest { (selected, dates) -> loadPeriod(uid, selected, dates.first, dates.second) }
            }
        }
    }

    fun refresh() { retry.value += 1 }
    fun selectFilter(value: DashboardPeriodFilter) { filter.value = value; if (value != DashboardPeriodFilter.CUSTOM) refresh() }
    fun onCustomStart(value: String) { customStart.value = value; _uiState.update { it.copy(customStart = value) } }
    fun onCustomEnd(value: String) { customEnd.value = value; _uiState.update { it.copy(customEnd = value) } }

    private suspend fun loadBusiness(uid: String) {
        businessRepository.getBusiness().fold({ business = it }, { error = it.salesUserMessage() })
        if (authRepository.currentUser.value?.uid == uid) render()
    }

    private suspend fun loadPeriod(uid: String, selected: DashboardPeriodFilter, start: String, end: String) {
        val range = when (selected) {
            DashboardPeriodFilter.WEEK -> DashboardPeriods.weekRange()
            DashboardPeriodFilter.MONTH -> DashboardPeriods.monthRange()
            DashboardPeriodFilter.CUSTOM -> if (DashboardPeriods.valid(start) && DashboardPeriods.valid(end) && start <= end) DashboardPeriods.customRange(start, end) else null
        }
        if (range == null) { error = "Selecciona un rango de fechas válido."; loading = false; render(); return }
        sales = emptyList(); monthlySales = emptyList(); expenses = emptyList(); error = null; fromCache = false; pendingLoads = 2; loading = salesRepository != null || expensesRepository != null; render()
        val salesSource = salesRepository; val expensesSource = expensesRepository
        if (salesSource == null || expensesSource == null) { loading = false; render(); return }
        coroutineScope {
            launch { salesSource.observeRange(uid, range.start, range.endExclusive).collect { result ->
                pendingLoads = (pendingLoads - 1).coerceAtLeast(0); loading = pendingLoads > 0
                result.fold({ data -> sales = data.value; fromCache = fromCache || data.fromCache }, { error = it.salesUserMessage() })
                render()
            } }
            launch { expensesSource.observeRange(uid, range.start, range.endExclusive).collect { result ->
                pendingLoads = (pendingLoads - 1).coerceAtLeast(0); loading = pendingLoads > 0
                result.fold({ data -> expenses = data.value; fromCache = fromCache || data.fromCache }, { error = it.salesUserMessage() })
                render()
            } }
            launch { salesSource.observeRange(uid, "${DashboardPeriods.currentMonth()}-01", DashboardPeriods.monthRange().endExclusive).collect { result ->
                result.fold({ data -> monthlySales = data.value; fromCache = fromCache || data.fromCache }, { error = it.salesUserMessage() })
                render()
            } }
        }
    }

    private fun render() {
        val selected = filter.value
        val rangeLabel = when (selected) { DashboardPeriodFilter.WEEK -> "Semana"; DashboardPeriodFilter.MONTH -> "Mes actual"; DashboardPeriodFilter.CUSTOM -> "Personalizado" }
        val salesTotal = sales.fold(0L) { total, item -> runCatching { Math.addExact(total, item.draft.total) }.getOrElse { total } }
        val expensesTotal = expenses.fold(0L) { total, item -> runCatching { Math.addExact(total, item.draft.amount) }.getOrElse { total } }
        val balance = salesTotal - expensesTotal
        val recent = (sales.map { MovementRow(it.draft.saleDate, it.createdAtMillis, it.draft.id, it.productName, "+${formatClp(it.draft.total)}", true) } +
            expenses.map { MovementRow(it.draft.expenseDate, it.createdAtMillis, it.draft.id, it.draft.description, "-${formatClp(it.draft.amount)}", false) })
            .sortedWith(compareByDescending<MovementRow> { it.date }.thenByDescending { it.createdAt }.thenBy { it.id }).take(5)
            .map { MovimientoUi(it.id, it.title, it.date, it.amount, it.income) }
        val monthlyGoal = business?.metaMensual ?: 0L
        val monthSales = monthlySales.sumOf { it.draft.total }
        _uiState.value = _uiState.value.copy(userEmail = authRepository.currentUser.value?.displayName?.takeIf { it.isNotBlank() } ?: authRepository.currentUser.value?.email.orEmpty(), nombreNegocio = business?.nombreNegocio.orEmpty(), metaMensual = monthlyGoal,
            ventasTotales = salesTotal, ventasMesActual = monthSales, gastosTotales = expensesTotal, ganancia = balance, balanceMovimientos = balance, margen = "—", movimientosRecientes = recent,
            selectedFilter = selected, periodLabel = rangeLabel, customStart = customStart.value, customEnd = customEnd.value,
            isLoading = loading, errorMessage = error, fromCache = fromCache)
    }

    private data class MovementRow(val date: String, val createdAt: Long, val id: String, val title: String, val amount: String, val income: Boolean)
}
