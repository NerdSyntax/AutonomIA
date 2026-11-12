package com.nerdsyntax.juntalucas.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.formatClp
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.Business
import com.nerdsyntax.juntalucas.feature.business.domain.BusinessRepository
import com.nerdsyntax.juntalucas.feature.dashboard.domain.DashboardDateRange
import com.nerdsyntax.juntalucas.feature.dashboard.domain.DashboardPeriodFilter
import com.nerdsyntax.juntalucas.feature.dashboard.domain.DashboardPeriods
import com.nerdsyntax.juntalucas.feature.movements.domain.Expense
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpensesRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.Sale
import com.nerdsyntax.juntalucas.feature.movements.domain.SaleDates
import com.nerdsyntax.juntalucas.feature.movements.domain.SalesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository,
    private val salesRepository: SalesRepository? = null,
    private val expensesRepository: ExpensesRepository? = null
) : ViewModel() {

    private val locale = Locale.forLanguageTag("es-CL")

    private val periodSelection = MutableStateFlow(
        currentMonthSelection()
    )

    private val retry = MutableStateFlow(0)
    private var planSimuladoTemporal: String? = null

    private val _uiState = MutableStateFlow(
        DashboardUiState(isLoading = true)
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser
                .map { it?.uid }
                .distinctUntilChanged()
                .collectLatest { uid ->
                    val initialSelection = currentMonthSelection()
                    periodSelection.value = initialSelection

                    _uiState.value = DashboardUiState(
                        userName = "Usuario",
                        selectedFilter = initialSelection.filter,
                        periodLabel = periodLabel(initialSelection),
                        customStart = initialSelection.start,
                        customEnd = initialSelection.end,
                        isLoading = uid != null,
                        graficosCargando = uid != null
                    )

                    if (uid == null) {
                        _uiState.update {
                            it.copy(
                                errorMessage = "Inicia sesión para ver tu negocio."
                            )
                        }
                        return@collectLatest
                    }
                    launch {
                        observeUserName(uid).collect { name ->
                            _uiState.update { it.copy(userName = name) }
                        }
                    }
                    launch {
                        observeSubscriptionPlan(uid).collect { plan ->
                            _uiState.update { it.copy(planActual = plan) }
                        }
                    }
                    combine(
                        periodSelection,
                        retry
                    ) { selection, _ ->
                        selection
                    }.collectLatest { selection ->
                        loadPeriod(uid, selection)
                    }
                }
        }
    }

    private fun observeUserName(uid: String): Flow<String> = callbackFlow {
        val authUser = authRepository.currentUser.value
        val rawAuthName = authUser?.displayName ?: authUser?.email?.substringBefore("@")
        val initialName = extraerPrimerNombre(rawAuthName) ?: "Usuario"

        trySend(initialName)

        val listener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val rawFirestore = listOf("name", "nombre", "firstName", "fullName", "displayName", "userName")
                        .firstNotNullOfOrNull { snapshot.getString(it) }
                    val firestoreName = extraerPrimerNombre(rawFirestore)
                    trySend(firestoreName ?: initialName)
                }
            }
        awaitClose { listener.remove() }
    }

    private fun observeSubscriptionPlan(uid: String): Flow<String> = callbackFlow {
        trySend("FREE")
        val listener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("business")
            .document("profile")
            .addSnapshotListener { snapshot, error ->
                if (planSimuladoTemporal != null) {
                    trySend(planSimuladoTemporal!!)
                    return@addSnapshotListener
                }

                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val susMap = snapshot.get("suscripcion") as? Map<*, *>
                    val plan = susMap?.get("plan") as? String ?: "FREE"
                    trySend(plan)
                }
            }
        awaitClose { listener.remove() }
    }

    private fun extraerPrimerNombre(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val limpio = raw.trim()
            .split(Regex("[\\s._0-9-]+"))
            .firstOrNull { it.length >= 2 && it.lowercase() !in setOf("el", "la", "los", "las", "de", "del", "mi", "usuario") }
            ?: return null
        return limpio.lowercase().replaceFirstChar { it.uppercase() }
    }

    fun refresh() {
        retry.update { it + 1 }
    }

    fun selectFilter(value: DashboardPeriodFilter) {
        val current = periodSelection.value

        val anchor = runCatching {
            LocalDate.parse(current.start)
        }.getOrElse {
            LocalDate.parse(DashboardPeriods.today())
        }

        if (value == DashboardPeriodFilter.CUSTOM) {
            applyPeriod(
                selected = value,
                start = current.start,
                end = current.end
            )
        } else {
            applyPeriod(
                selected = value,
                start = anchor.toString(),
                end = anchor.toString()
            )
        }
    }

    fun applyPeriod(
        selected: DashboardPeriodFilter,
        start: String,
        end: String
    ) {
        val normalized = runCatching {
            val anchor = LocalDate.parse(start)

            val first = when (selected) {
                DashboardPeriodFilter.WEEK -> anchor.minusDays((anchor.dayOfWeek.value - 1).toLong())
                DashboardPeriodFilter.MONTH -> anchor.withDayOfMonth(1)
                DashboardPeriodFilter.YEAR -> anchor.withDayOfYear(1)
                DashboardPeriodFilter.CUSTOM -> anchor
            }

            val last = when (selected) {
                DashboardPeriodFilter.WEEK -> first.plusDays(6)
                DashboardPeriodFilter.MONTH -> first.plusMonths(1).minusDays(1)
                DashboardPeriodFilter.YEAR -> first.plusYears(1).minusDays(1)
                DashboardPeriodFilter.CUSTOM -> LocalDate.parse(end)
            }

            require(!last.isBefore(first))

            PeriodSelection(
                filter = selected,
                start = first.toString(),
                end = last.toString()
            )
        }.getOrNull()

        if (normalized == null) {
            _uiState.update {
                it.copy(
                    errorMessage = "Selecciona un rango de fechas válido."
                )
            }
            return
        }

        periodSelection.value = normalized
    }

    fun onCustomStart(value: String) {
        _uiState.update { it.copy(customStart = value) }
    }

    fun onCustomEnd(value: String) {
        _uiState.update { it.copy(customEnd = value) }
    }

    fun mostrarPaywall() {
        _uiState.update { it.copy(mostrarPaywallSuscripcion = true, mensajePaywallPersonalizado = null) }
    }

    fun ocultarPaywall() {
        _uiState.update { it.copy(mostrarPaywallSuscripcion = false, mensajePaywallPersonalizado = null) }
    }
    fun simularCompra(planId: String) {
        val uid = authRepository.currentUser.value?.uid ?: return
        planSimuladoTemporal = planId
        _uiState.update { it.copy(planActual = planId) }
        ocultarPaywall()

        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val profileRef = db.collection("users").document(uid).collection("business").document("profile")
                val data = mapOf("suscripcion" to mapOf("plan" to planId))
                profileRef.set(data, SetOptions.merge()).await()
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Escritura bloqueada por reglas de Firebase: ${e.message}")
            }
        }
    }

    fun exportarDatosCsv(context: android.content.Context) {
        if (salesRepository == null || expensesRepository == null) {
            _uiState.update { it.copy(errorMessage = "Datos de repositorio no disponibles para exportar.") }
            return
        }

        viewModelScope.launch {
            try {
                val uid = authRepository.currentUser.value?.uid
                if (uid == null) {
                    _uiState.update { it.copy(errorMessage = "Debes iniciar sesión para exportar.") }
                    return@launch
                }

                val currentPlan = _uiState.value.planActual
                val selectedFilter = periodSelection.value.filter
                val startDate = LocalDate.parse(periodSelection.value.start)
                val today = LocalDate.parse(DashboardPeriods.today())

                val isCurrentMonth = startDate.year == today.year && startDate.month == today.month
                val sixMonthsAgo = today.minusMonths(5).withDayOfMonth(1)

                if (currentPlan == "FREE") {
                    if (!isCurrentMonth || selectedFilter == DashboardPeriodFilter.YEAR || selectedFilter == DashboardPeriodFilter.CUSTOM) {
                        _uiState.update {
                            it.copy(
                                mostrarPaywallSuscripcion = true,
                                mensajePaywallPersonalizado = "Tu plan Gratis solo te permite exportar el mes en curso. Para descargar historial de meses anteriores, mejora tu plan a Plus o Pro."
                            )
                        }
                        return@launch
                    }
                }

                if (currentPlan == "PLUS") {
                    if (startDate.isBefore(sixMonthsAgo) || selectedFilter == DashboardPeriodFilter.YEAR || selectedFilter == DashboardPeriodFilter.CUSTOM) {
                        _uiState.update {
                            it.copy(
                                mostrarPaywallSuscripcion = true,
                                mensajePaywallPersonalizado = "Tu plan Plus permite exportar hasta los últimos 6 meses. La exportación histórica completa es exclusiva del plan Pro."
                            )
                        }
                        return@launch
                    }
                }

                val nombreNegocio = _uiState.value.nombreNegocio.ifBlank { "Mi Negocio" }
                val metaMensual = _uiState.value.metaMensual

                val start = periodSelection.value.start
                val endExclusive = LocalDate.parse(periodSelection.value.end).plusDays(1).toString()

                val salesResult = salesRepository.observeRange(uid, start, endExclusive).first()
                val expensesResult = expensesRepository.observeRange(uid, start, endExclusive).first()

                val sales = salesResult.getOrNull()?.value ?: emptyList()
                val expenses = expensesResult.getOrNull()?.value ?: emptyList()

                val csvContent = buildString {
                    appendLine("sep=;")
                    appendLine("REPORTE FINANCIERO - ${nombreNegocio.uppercase()}")
                    appendLine("Periodo exportado:;${start} al ${periodSelection.value.end}")
                    appendLine("Meta mensual configurada:;$metaMensual")
                    appendLine()

                    appendLine("VENTAS;;;;;GASTOS;;;;")
                    appendLine("FECHA;PRODUCTO;MONTO;MEDIO PAGO;;FECHA;DESCRIPCION;CATEGORIA;MONTO;MEDIO PAGO")

                    val sortedSales = sales.sortedByDescending { it.draft.saleDate }
                    val sortedExpenses = expenses.sortedByDescending { it.draft.expenseDate }

                    val maxRows = maxOf(sortedSales.size, sortedExpenses.size)

                    for (i in 0 until maxRows) {
                        val sale = sortedSales.getOrNull(i)
                        val expense = sortedExpenses.getOrNull(i)

                        val saleCols = if (sale != null) {
                            val nombre = sale.productName.replace(";", ",")
                            "${sale.draft.saleDate};$nombre;${sale.draft.total};${sale.draft.paymentMethod.label}"
                        } else {
                            ";;;"
                        }

                        val sepCol = ""

                        val expenseCols = if (expense != null) {
                            val desc = expense.draft.description.replace(";", ",")
                            val cat = expense.draft.category.replace(";", ",")
                            "${expense.draft.expenseDate};$desc;$cat;${expense.draft.amount};${expense.draft.paymentMethod.label}"
                        } else {
                            ";;;;"
                        }

                        appendLine("$saleCols;$sepCol;$expenseCols")
                    }
                }

                val fileName = "Reporte_${nombreNegocio.replace(" ", "_")}_${start}_${periodSelection.value.end}.csv"
                val file = java.io.File(context.cacheDir, fileName)
                file.writeText("\uFEFF$csvContent", Charsets.UTF_8)

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Reporte Financiero ${periodSelection.value.end}")
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(android.content.Intent.createChooser(intent, "Exportar movimientos"))

            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Error al generar CSV", e)
                _uiState.update { it.copy(errorMessage = "Error al exportar los datos.") }
            }
        }
    }

    private fun currentMonthSelection(): PeriodSelection {
        val today = LocalDate.parse(DashboardPeriods.today())
        val first = today.withDayOfMonth(1)

        return PeriodSelection(
            filter = DashboardPeriodFilter.MONTH,
            start = first.toString(),
            end = first.plusMonths(1).minusDays(1).toString()
        )
    }

    private fun resolveRange(
        selection: PeriodSelection
    ): DashboardDateRange? {
        return runCatching {
            val start = LocalDate.parse(selection.start)
            val end = LocalDate.parse(selection.end)
            require(!end.isBefore(start))
            DashboardDateRange(start = start.toString(), endExclusive = end.plusDays(1).toString())
        }.getOrNull()
    }

    private fun periodLabel(selection: PeriodSelection): String {
        val range = resolveRange(selection) ?: return "Elegir fechas"
        val start = LocalDate.parse(range.start)
        val end = LocalDate.parse(range.endExclusive).minusDays(1)

        return when (selection.filter) {
            DashboardPeriodFilter.WEEK -> {
                if (start.year == end.year && start.month == end.month) {
                    val format = DateTimeFormatter.ofPattern("MMM yy", locale)
                    "${start.dayOfMonth}–${end.dayOfMonth} " + end.format(format).replace(".", "")
                } else {
                    val format = DateTimeFormatter.ofPattern("d/M/yy", locale)
                    "${start.format(format)}–${end.format(format)}"
                }
            }
            DashboardPeriodFilter.MONTH -> {
                start.format(DateTimeFormatter.ofPattern("MMM yyyy", locale)).replace(".", "").replaceFirstChar { it.uppercase(locale) }
            }
            DashboardPeriodFilter.YEAR -> start.year.toString()
            DashboardPeriodFilter.CUSTOM -> {
                val format = DateTimeFormatter.ofPattern("d/M/yy", locale)
                "${start.format(format)}–${end.format(format)}"
            }
        }
    }

    private suspend fun loadPeriod(
        uid: String,
        selection: PeriodSelection
    ) {
        _uiState.update {
            it.copy(
                selectedFilter = selection.filter,
                periodLabel = periodLabel(selection),
                customStart = selection.start,
                customEnd = selection.end,
                ventasTotales = 0L,
                ventasMesActual = 0L,
                gastosTotales = 0L,
                ganancia = 0L,
                gananciaBruta = null,
                balanceMovimientos = 0L,
                margen = "Cargando…",
                movimientosRecientes = emptyList(),
                mesesGrafico = emptyList(),
                distribucionGastos = emptyList(),
                ventasPorProducto = emptyList(),
                errorMessage = null,
                errorGraficos = null,
                isLoading = true,
                graficosCargando = true,
                fromCache = false,
                graficosDesdeCache = false
            )
        }

        try {
            val range = resolveRange(selection)

            if (range == null) {
                showError("Selecciona un rango de fechas válido.")
                return
            }

            val salesSource = salesRepository
            val expensesSource = expensesRepository

            if (salesSource == null || expensesSource == null) {
                showError("No se pudieron conectar los datos del negocio.")
                return
            }

            val businessResult = businessRepository.getBusiness()
            if (authRepository.currentUser.value?.uid != uid) return

            val business = businessResult.getOrNull()
            val businessError = businessResult.exceptionOrNull()?.salesUserMessage()

            _uiState.update {
                it.copy(
                    nombreNegocio = business?.nombreNegocio.orEmpty(),
                    metaMensual = business?.metaMensual ?: 0L
                )
            }

            val currentMonthRange = DashboardPeriods.monthRange()

            val periodData = combine(
                salesSource.observeRange(uid, range.start, range.endExclusive),
                expensesSource.observeRange(uid, range.start, range.endExclusive)
            ) { salesResult, expensesResult ->
                salesResult to expensesResult
            }

            combine(
                periodData,
                salesSource.observeRange(uid, currentMonthRange.start, currentMonthRange.endExclusive)
            ) { periodResults, monthlyResult ->
                Triple(periodResults.first, periodResults.second, monthlyResult)
            }.collect { (salesResult, expensesResult, monthlyResult) ->
                if (authRepository.currentUser.value?.uid != uid) return@collect

                val periodError = salesResult.exceptionOrNull() ?: expensesResult.exceptionOrNull()
                if (periodError != null) {
                    showError(periodError.salesUserMessage())
                    return@collect
                }

                val salesData = salesResult.getOrThrow()
                val expensesData = expensesResult.getOrThrow()
                val monthlyData = monthlyResult.getOrNull()
                val additionalError = monthlyResult.exceptionOrNull()?.salesUserMessage() ?: businessError

                render(
                    selection = selection,
                    range = range,
                    business = business,
                    sales = salesData.value,
                    expenses = expensesData.value,
                    monthlySales = monthlyData?.value.orEmpty(),
                    fromCache = salesData.fromCache || expensesData.fromCache || (monthlyData?.fromCache == true),
                    periodFromCache = salesData.fromCache || expensesData.fromCache,
                    additionalError = additionalError
                )
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            if (authRepository.currentUser.value?.uid == uid) {
                showError(error.salesUserMessage())
            }
        }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                graficosCargando = false,
                errorMessage = message,
                errorGraficos = message,
                ventasTotales = 0L,
                ventasMesActual = 0L,
                gastosTotales = 0L,
                ganancia = 0L,
                gananciaBruta = null,
                balanceMovimientos = 0L,
                margen = "No disponible",
                movimientosRecientes = emptyList(),
                mesesGrafico = emptyList(),
                distribucionGastos = emptyList(),
                ventasPorProducto = emptyList()
            )
        }
    }

    private fun grossProfit(sales: List<Sale>): Double? {
        if (sales.isEmpty() || sales.any { it.unitCost == null }) {
            return null
        }

        val total = sales.sumOf { it.draft.total }

        val costs = sales.sumOf {
            requireNotNull(it.unitCost).toDouble() *
                    it.draft.quantity.toDouble()
        }

        return total.toDouble() - costs
    }

    private fun buildExpenseDistribution(
        expenses: List<Expense>
    ): List<GastoCategoriaUi> {
        val groups = expenses.groupBy { expense ->
            expense.draft.category
                .trim()
                .replace(Regex("\\s+"), " ")
                .ifBlank { "Sin categoría" }
                .lowercase(locale)
        }

        return groups.map { (_, categoryExpenses) ->
            val categoryName = categoryExpenses.first().draft.category
                .trim()
                .replace(Regex("\\s+"), " ")
                .ifBlank { "Sin categoría" }
                .replaceFirstChar { it.uppercase(locale) }

            GastoCategoriaUi(
                categoria = categoryName,
                monto = categoryExpenses.sumOf {
                    it.draft.amount
                }
            )
        }.sortedWith(
            compareByDescending<GastoCategoriaUi> { it.monto }
                .thenBy { it.categoria }
        )
    }

    private fun render(
        selection: PeriodSelection,
        range: DashboardDateRange,
        business: Business?,
        sales: List<Sale>,
        expenses: List<Expense>,
        monthlySales: List<Sale>,
        fromCache: Boolean,
        periodFromCache: Boolean,
        additionalError: String?
    ) {
        val salesTotal = sales.sumOf { it.draft.total }
        val expensesTotal = expenses.sumOf { it.draft.amount }
        val balance = salesTotal - expensesTotal
        val grossProfit = grossProfit(sales)

        val margin = when {
            sales.isEmpty() -> "Sin ventas"
            salesTotal <= 0L -> "No disponible"
            grossProfit == null -> "Faltan costos"

            else -> String.format(
                locale,
                "%.1f %%",
                grossProfit / salesTotal.toDouble() * 100.0
            )
        }

        val recent = (
                sales.map {
                    MovementRow(
                        date = it.draft.saleDate,
                        createdAt = it.createdAtMillis,
                        id = "venta_${it.draft.id}",
                        title = it.productName,
                        amount = "+${formatClp(it.draft.total)}",
                        income = true
                    )
                } +
                        expenses.map {
                            MovementRow(
                                date = it.draft.expenseDate,
                                createdAt = it.createdAtMillis,
                                id = "gasto_${it.draft.id}",
                                title = it.draft.description,
                                amount = "-${formatClp(it.draft.amount)}",
                                income = false
                            )
                        }
                )
            .sortedWith(
                compareByDescending<MovementRow> { it.date }
                    .thenByDescending { it.createdAt }
                    .thenBy { it.id }
            )
            .take(5)
            .map {
                MovimientoUi(
                    id = it.id,
                    titulo = it.title,
                    fecha = it.date,
                    montoStr = it.amount,
                    esIngreso = it.income
                )
            }

        val chartData = buildChartData(
            selection = selection,
            range = range,
            sales = sales,
            expenses = expenses
        )

        val expenseDistribution = buildExpenseDistribution(expenses)

        val productDistribution = sales
            .groupBy { it.draft.productId }
            .map { (productId, productSales) ->
                val latestSale = productSales.maxWithOrNull(
                    compareBy<Sale> { it.draft.saleDate }
                        .thenBy { it.createdAtMillis }
                )!!

                VentaProductoUi(
                    id = productId,
                    nombre = latestSale.productName,
                    monto = productSales.sumOf { it.draft.total }
                )
            }
            .sortedWith(
                compareByDescending<VentaProductoUi> { it.monto }
                    .thenBy { it.nombre }
            )

        _uiState.update {
            it.copy(
                nombreNegocio = business?.nombreNegocio.orEmpty(),
                metaMensual = business?.metaMensual ?: 0L,
                ventasTotales = salesTotal,
                ventasMesActual = monthlySales.sumOf {
                        sale -> sale.draft.total
                },
                gastosTotales = expensesTotal,
                ganancia = balance,
                balanceMovimientos = balance,
                gananciaBruta = grossProfit,
                margen = margin,
                movimientosRecientes = recent,
                selectedFilter = selection.filter,
                periodLabel = periodLabel(selection),
                customStart = selection.start,
                customEnd = selection.end,
                isLoading = false,
                errorMessage = additionalError,
                fromCache = fromCache,
                mesesGrafico = chartData,
                graficosCargando = false,
                errorGraficos = null,
                graficosDesdeCache = periodFromCache,
                distribucionGastos = expenseDistribution,
                ventasPorProducto = productDistribution
            )
        }
    }

    fun cargarDatosDePrueba() {
        val uid = authRepository.currentUser.value?.uid ?: return
        val fechaDestino = periodSelection.value.start

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                com.nerdsyntax.juntalucas.feature.onboarding.data.OnboardingSampleData.generarDatosDePrueba(uid, fechaDestino)
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Error Firebase", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error de Firebase: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun buildChartData(
        selection: PeriodSelection,
        range: DashboardDateRange,
        sales: List<Sale>,
        expenses: List<Expense>
    ): List<MesGraficoUi> {
        if (sales.isEmpty() && expenses.isEmpty()) {
            return emptyList()
        }

        val start = LocalDate.parse(range.start)
        val endExclusive = LocalDate.parse(range.endExclusive)
        val days = ChronoUnit.DAYS.between(start, endExclusive)

        val groupByMonth =
            selection.filter == DashboardPeriodFilter.YEAR ||
                    (
                            selection.filter == DashboardPeriodFilter.CUSTOM &&
                                    days > 62
                            )

        fun key(date: String): String {
            return if (groupByMonth) {
                date.take(7)
            } else {
                date
            }
        }

        val salesByPeriod = sales.groupBy {
            key(it.draft.saleDate)
        }

        val expensesByPeriod = expenses.groupBy {
            key(it.draft.expenseDate)
        }

        val periods = mutableListOf<String>()

        if (groupByMonth) {
            var month = YearMonth.from(start)
            val lastMonth = YearMonth.from(
                endExclusive.minusDays(1)
            )

            while (!month.isAfter(lastMonth)) {
                periods.add(month.toString())
                month = month.plusMonths(1)
            }
        } else {
            var day = start

            while (day.isBefore(endExclusive)) {
                periods.add(day.toString())
                day = day.plusDays(1)
            }
        }

        val monthFormatter = DateTimeFormatter.ofPattern(
            "MMM yy",
            locale
        )
        val dayFormatter = DateTimeFormatter.ofPattern(
            "dd/MM",
            locale
        )

        return periods.map { period ->
            val periodSales = salesByPeriod[period].orEmpty()
            val periodExpenses = expensesByPeriod[period].orEmpty()

            val label = if (groupByMonth) {
                YearMonth.parse(period)
                    .format(monthFormatter)
                    .replace(".", "")
                    .replaceFirstChar { it.uppercase(locale) }
            } else {
                LocalDate.parse(period).format(dayFormatter)
            }

            MesGraficoUi(
                periodo = period,
                etiqueta = label,
                ventas = periodSales.sumOf { it.draft.total },
                gastos = periodExpenses.sumOf { it.draft.amount },
                gananciaBruta = grossProfit(periodSales),
                tieneVentas = periodSales.isNotEmpty()
            )
        }
    }

    private data class PeriodSelection(
        val filter: DashboardPeriodFilter,
        val start: String,
        val end: String
    )

    private data class MovementRow(
        val date: String,
        val createdAt: Long,
        val id: String,
        val title: String,
        val amount: String,
        val income: Boolean
    )
}