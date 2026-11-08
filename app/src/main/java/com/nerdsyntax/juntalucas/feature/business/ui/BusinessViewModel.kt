package com.nerdsyntax.juntalucas.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.formatClp
import com.nerdsyntax.juntalucas.core.format.parseClp
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.*
import com.nerdsyntax.juntalucas.feature.movements.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReusableExpensesUiState(
    val items: List<ReusableExpense> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val fromCache: Boolean = false
)

class BusinessViewModel(
    private val auth: AuthRepository,
    private val businessRepository: BusinessRepository,
    private val catalogRepository: CatalogRepository,
    private val salesRepository: SalesRepository,
    private val reusableExpensesRepository: ReusableExpensesRepository? = null
) : ViewModel() {

    private val retry = MutableStateFlow(0)

    private var allProducts = emptyList<CatalogProduct>()
    private var business: Business? = null
    private var monthlySales = emptyList<Sale>()
    private var loadError: String? = null
    private var loading = true

    private val _uiState = MutableStateFlow(BusinessUiState())
    val uiState = _uiState.asStateFlow()

    private val reusableExpensesRetry = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val reusableExpensesUiState: StateFlow<ReusableExpensesUiState> = combine(
        auth.currentUser.map { it?.uid }.distinctUntilChanged(),
        uiState.map { it.selectedTab }.distinctUntilChanged(),
        reusableExpensesRetry
    ) { uid, tab, _ ->
        uid to tab
    }.flatMapLatest { (uid, tab) ->
        if (tab == BusinessTab.GASTOS) {
            observeReusableExpenses(uid)
        } else {
            flowOf(ReusableExpensesUiState())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = 0,
            replayExpirationMillis = 0
        ),
        initialValue = ReusableExpensesUiState()
    )

    private fun observeReusableExpenses(uid: String?): Flow<ReusableExpensesUiState> {
        if (uid == null) {
            return flowOf(
                ReusableExpensesUiState(
                    isLoading = false,
                    errorMessage = "Inicia sesión para ver tus gastos reutilizables."
                )
            )
        }

        val repository = reusableExpensesRepository ?: return flowOf(
            ReusableExpensesUiState(
                isLoading = false,
                errorMessage = "El catálogo de gastos no está disponible."
            )
        )

        return flow {
            emit(ReusableExpensesUiState())

            repository.observeAll(uid).collect { result ->
                if (auth.currentUser.value?.uid == uid) {
                    emit(
                        result.fold(
                            onSuccess = { data ->
                                ReusableExpensesUiState(
                                    items = data.value,
                                    isLoading = false,
                                    fromCache = data.fromCache
                                )
                            },
                            onFailure = { error ->
                                ReusableExpensesUiState(
                                    isLoading = false,
                                    errorMessage = error.salesUserMessage()
                                )
                            }
                        )
                    )
                }
            }
        }.catch { error ->
            emit(
                ReusableExpensesUiState(
                    isLoading = false,
                    errorMessage = error.salesUserMessage()
                )
            )
        }
    }

    fun retryReusableExpenses() {
        reusableExpensesRetry.update { it + 1 }
    }

    suspend fun createReusableExpense(
        requestId: String,
        name: String,
        category: String,
        suggestedAmountText: String
    ): Result<Unit> {
        val uid = auth.currentUser.value?.uid ?: return Result.failure(
            DataValidationException("Inicia sesión para guardar un gasto reutilizable.")
        )
        val repository = reusableExpensesRepository ?: return Result.failure(
            DataValidationException("El catálogo de gastos no está disponible.")
        )

        val amount = if (suggestedAmountText.isBlank()) {
            null
        } else {
            parseClp(suggestedAmountText.trim()) ?: return Result.failure(
                DataValidationException("Ingresa un monto válido en CLP o déjalo vacío.")
            )
        }

        val expense = ReusableExpense(
            id = requestId,
            userId = uid,
            name = name.trim(),
            category = category.trim(),
            suggestedAmount = amount
        )

        return repository.create(expense)
    }

    init {
        viewModelScope.launch {
            auth.currentUser
                .map { it?.uid }
                .distinctUntilChanged()
                .combine(retry) { uid, _ -> uid }
                .collectLatest { uid ->
                    allProducts = emptyList()
                    business = null
                    monthlySales = emptyList()
                    loadError = null
                    loading = uid != null

                    render()

                    if (uid == null) {
                        loadError = "Inicia sesión para ver tu negocio."
                        loading = false
                        render()
                        return@collectLatest
                    }

                    // Cancela estas observaciones al cambiar de usuario
                    // o al solicitar un nuevo intento.
                    coroutineScope {
                        launch {
                            businessRepository.getBusiness().fold(
                                onSuccess = {
                                    business = it
                                },
                                onFailure = {
                                    loadError = it.salesUserMessage()
                                }
                            )

                            render()
                        }

                        launch {
                            catalogRepository.observeAll(uid).collect { result ->
                                result.fold(
                                    onSuccess = {
                                        allProducts = it.value
                                    },
                                    onFailure = {
                                        loadError = it.salesUserMessage()
                                    }
                                )

                                loading = false
                                render()
                            }
                        }

                        launch {
                            val currentMonth = SaleDates.today().take(7)

                            salesRepository
                                .observeMonth(uid, currentMonth)
                                .collect { result ->
                                    result.fold(
                                        onSuccess = {
                                            monthlySales = it.value
                                        },
                                        onFailure = {
                                            loadError = it.salesUserMessage()
                                        }
                                    )

                                    render()
                                }
                        }
                    }
                }
        }
    }

    fun onTabSelected(tab: BusinessTab) {
        _uiState.update {
            it.copy(selectedTab = tab)
        }

        if (
            tab == BusinessTab.INFORMACION &&
            !_uiState.value.isEditingBusiness
        ) {
            refreshBusiness()
        }
    }

    fun onSearchChange(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
        }

        render()
    }

    fun onFilterSelected(filter: String) {
        _uiState.update {
            it.copy(selectedFilter = filter)
        }

        render()
    }

    fun retry() {
        retry.value += 1
    }

    fun startEditingBusiness() {
        _uiState.update {
            it.copy(
                isEditingBusiness = true,
                errorMessage = null
            )
        }
    }

    fun cancelEditingBusiness() {
        _uiState.update {
            it.copy(
                isEditingBusiness = false,
                errorMessage = null
            )
        }

        render()
    }

    fun onBusinessNameChange(value: String) {
        _uiState.update {
            it.copy(businessName = value)
        }
    }

    fun onBusinessRubroChange(value: String) {
        _uiState.update {
            it.copy(rubro = value)
        }
    }

    fun onBusinessRegionChange(value: String) {
        _uiState.update {
            it.copy(region = value)
        }
    }

    fun onBusinessComunaChange(value: String) {
        _uiState.update {
            it.copy(comuna = value)
        }
    }

    fun onBusinessMetaChange(value: String) {
        _uiState.update {
            it.copy(
                metaMensual = value
                    .filter { character -> character.isDigit() }
                    .take(12)
            )
        }
    }

    fun saveBusiness() {
        val state = _uiState.value

        if (state.isSavingBusiness) return

        val nombre = state.businessName.trim()

        val meta = state.metaMensual
            .filter { it.isDigit() }
            .toLongOrNull()
            ?: 0L

        if (nombre.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Ingresa el nombre de tu negocio."
                )
            }

            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSavingBusiness = true,
                    errorMessage = null
                )
            }

            val base = businessRepository
                .getBusiness()
                .getOrNull()
                ?: business

            if (base == null) {
                _uiState.update {
                    it.copy(
                        isSavingBusiness = false,
                        errorMessage = "No se pudieron cargar los datos " +
                                "de tu negocio. Inténtalo de nuevo."
                    )
                }

                return@launch
            }

            val actualizado = base.copy(
                nombreNegocio = nombre,
                rubro = state.rubro.trim(),
                region = state.region.trim(),
                comuna = state.comuna.trim(),
                metaMensual = meta
            )

            businessRepository.saveBusiness(actualizado).fold(
                onSuccess = {
                    business = actualizado

                    _uiState.update {
                        it.copy(
                            isSavingBusiness = false,
                            isEditingBusiness = false
                        )
                    }

                    render()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSavingBusiness = false,
                            errorMessage = error.salesUserMessage()
                        )
                    }
                }
            )
        }
    }

    private fun refreshBusiness() {
        viewModelScope.launch {
            businessRepository.getBusiness().onSuccess { fresh ->
                if (fresh != null) {
                    business = fresh
                    render()
                }
            }
        }
    }

    fun toggleActive(productId: String) {
        val product = allProducts.firstOrNull {
            it.id == productId
        } ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            catalogRepository.update(
                product.copy(active = !product.active)
            ).fold(
                onSuccess = {
                    retry()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.salesUserMessage()
                        )
                    }
                }
            )
        }
    }

    private fun needsStockAlert(product: CatalogProduct): Boolean {
        return product.isProduct &&
                !product.isAsset &&
                product.active &&
                product.trackStock &&
                product.stock <= product.stockMinimum
    }

    private fun toProductItem(product: CatalogProduct): ProductItem {
        val lowStock = needsStockAlert(product)

        val margin = product.unitCost?.let { cost ->
            if (product.price > 0L) {
                val percentage =
                    (product.price - cost).toDouble() /
                            product.price * 100

                "${percentage.toInt()}%"
            } else {
                "—"
            }
        } ?: "—"

        val unit = product.unit.ifBlank { "unidad" }

        return ProductItem(
            id = product.id,
            name = product.name,
            type = when {
                product.isAsset -> "Activo"
                product.isProduct -> "Producto"
                else -> "Servicio"
            },
            price = formatClp(product.price),
            cost = product.unitCost?.let(::formatClp) ?: "—",
            margin = margin,
            stock = if (product.isProduct && product.trackStock) {
                "${product.stock} $unit"
            } else {
                "—"
            },
            active = product.active,
            stockMinimum = "${product.stockMinimum} $unit",
            lowStock = lowStock,
            outOfStock = lowStock && product.stock == 0L,
            photoStoragePath = product.photoStoragePath
        )
    }

    private fun render() {
        val state = _uiState.value
        val query = state.searchQuery.trim()

        val visible = allProducts.filter { product ->
            val matchesFilter = when (state.selectedFilter) {
                "Todos" -> true

                "Productos" ->
                    product.isProduct && !product.isAsset

                "Servicios" ->
                    !product.isProduct && !product.isAsset

                "Activos" ->
                    product.isAsset

                "Stock bajo" ->
                    needsStockAlert(product)

                else -> true
            }

            val matchesQuery = query.isBlank() ||
                    product.name.contains(
                        query,
                        ignoreCase = true
                    ) ||
                    product.category.contains(
                        query,
                        ignoreCase = true
                    )

            matchesFilter && matchesQuery
        }.sortedBy {
            it.name.lowercase()
        }

        // Incluye todas las alertas, independientemente
        // de la búsqueda o del filtro del catálogo.
        val stockAlerts = allProducts
            .filter(::needsStockAlert)
            .map(::toProductItem)
            .sortedWith(
                compareBy<ProductItem> { !it.outOfStock }
                    .thenBy { it.name.lowercase() }
            )

        val goal = business?.metaMensual ?: 0L
        val total = monthlySales.sumOf { it.draft.total }

        val percentage = if (goal > 0L) {
            (total.toDouble() / goal)
                .coerceIn(0.0, 1.0)
                .toFloat()
        } else {
            0f
        }

        val activity = business?.tipoActividad
            .orEmpty()
            .replaceFirstChar { it.uppercase() }

        _uiState.value = state.copy(
            products = visible.map(::toProductItem),
            stockAlerts = stockAlerts,
            isLoading = loading,
            errorMessage = loadError,

            businessName = if (state.isEditingBusiness) {
                state.businessName
            } else {
                business?.nombreNegocio.orEmpty()
            },

            businessDetails = listOf(
                business?.rubro,
                business?.region,
                business?.comuna
            ).filter {
                !it.isNullOrBlank()
            }.joinToString(" · "),

            productCount = allProducts.count {
                it.isProduct && !it.isAsset
            }.toString(),

            serviceCount = allProducts.count {
                !it.isProduct && !it.isAsset
            }.toString(),

            assetCount = allProducts.count {
                it.isAsset
            }.toString(),

            goalTotal = formatClp(goal),

            goalProgressText =
                "${formatClp(total)} alcanzados este mes " +
                        "(${(percentage * 100).toInt()}%)",

            goalPercentage = percentage,

            rubro = if (state.isEditingBusiness) {
                state.rubro
            } else {
                business?.rubro.orEmpty()
            },

            actividad = activity,

            region = if (state.isEditingBusiness) {
                state.region
            } else {
                business?.region.orEmpty()
            },

            comuna = if (state.isEditingBusiness) {
                state.comuna
            } else {
                business?.comuna.orEmpty()
            },

            metaMensual = if (state.isEditingBusiness) {
                state.metaMensual
            } else {
                business?.metaMensual
                    ?.takeIf { it > 0L }
                    ?.toString()
                    .orEmpty()
            },

            registro = if (business != null) {
                "Configurado"
            } else {
                ""
            }
        )
    }
}
