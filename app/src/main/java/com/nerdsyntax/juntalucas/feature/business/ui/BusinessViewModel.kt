package com.nerdsyntax.juntalucas.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerdsyntax.juntalucas.core.data.salesUserMessage
import com.nerdsyntax.juntalucas.core.format.formatClp
import com.nerdsyntax.juntalucas.feature.auth.domain.repository.AuthRepository
import com.nerdsyntax.juntalucas.feature.business.domain.*
import com.nerdsyntax.juntalucas.feature.movements.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BusinessViewModel(
    private val auth: AuthRepository,
    private val businessRepository: BusinessRepository,
    private val catalogRepository: CatalogRepository,
    private val salesRepository: SalesRepository
) : ViewModel() {
    private val retry = MutableStateFlow(0)
    private var allProducts = emptyList<CatalogProduct>()
    private var business: Business? = null
    private var monthlySales = emptyList<Sale>()
    private var loadError: String? = null
    private var loading = true
    private val _uiState = MutableStateFlow(BusinessUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            auth.currentUser.map { it?.uid }.distinctUntilChanged().combine(retry) { uid, _ -> uid }
                .collectLatest { uid ->
                    allProducts = emptyList(); business = null; monthlySales = emptyList(); loadError = null
                    loading = uid != null; render()
                    if (uid == null) { loadError = "Inicia sesión para ver tu negocio."; loading = false; render(); return@collectLatest }
                    launch {
                        businessRepository.getBusiness().fold({ business = it }, { loadError = it.salesUserMessage() })
                        render()
                    }
                    launch {
                        catalogRepository.observeAll(uid).collect { result ->
                            result.fold({ allProducts = it.value }, { loadError = it.salesUserMessage() })
                            loading = false; render()
                        }
                    }
                    launch {
                        salesRepository.observeMonth(uid, SaleDates.today().take(7)).collect { result ->
                            result.fold({ monthlySales = it.value }, { loadError = it.salesUserMessage() })
                            render()
                        }
                    }
                }
        }
    }

    fun onTabSelected(tab: BusinessTab) { _uiState.update { it.copy(selectedTab = tab) } }
    fun onSearchChange(query: String) { _uiState.update { it.copy(searchQuery = query) }; render() }
    fun onFilterSelected(filter: String) { _uiState.update { it.copy(selectedFilter = filter) }; render() }
    fun retry() { retry.value += 1 }
    fun startEditingBusiness() { _uiState.update { it.copy(isEditingBusiness = true, errorMessage = null) } }
    fun cancelEditingBusiness() { render() }
    fun onBusinessNameChange(value: String) { _uiState.update { it.copy(businessName = value) } }
    fun onBusinessRubroChange(value: String) { _uiState.update { it.copy(rubro = value) } }
    fun onBusinessRegionChange(value: String) { _uiState.update { it.copy(region = value) } }
    fun onBusinessComunaChange(value: String) { _uiState.update { it.copy(comuna = value) } }
    fun saveBusiness() {
        val current = business ?: return
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingBusiness = true, errorMessage = null) }
            businessRepository.saveBusiness(current.copy(nombreNegocio = state.businessName.trim(), rubro = state.rubro.trim(), region = state.region.trim(), comuna = state.comuna.trim()))
                .fold({ business = current.copy(nombreNegocio = state.businessName.trim(), rubro = state.rubro.trim(), region = state.region.trim(), comuna = state.comuna.trim()); _uiState.update { it.copy(isSavingBusiness = false, isEditingBusiness = false) }; render() },
                    { error -> _uiState.update { it.copy(isSavingBusiness = false, errorMessage = error.salesUserMessage()) } })
        }
    }

    fun toggleActive(productId: String) {
        val product = allProducts.firstOrNull { it.id == productId } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            catalogRepository.update(product.copy(active = !product.active)).fold(
                { retry() },
                { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.salesUserMessage()) } }
            )
        }
    }

    private fun render() {
        val state = _uiState.value
        val query = state.searchQuery.trim()
        val visible = allProducts.filter { product ->
            (state.selectedFilter == "Todos" ||
                (state.selectedFilter == "Productos" && product.isProduct && !product.isAsset) ||
                (state.selectedFilter == "Servicios" && !product.isProduct && !product.isAsset) ||
                (state.selectedFilter == "Activos" && product.isAsset)) &&
                (query.isBlank() || product.name.contains(query, true) || product.category.contains(query, true))
        }.sortedBy { it.name.lowercase() }
        val goal = business?.metaMensual ?: 0L
        val total = monthlySales.sumOf { it.draft.total }
        val percentage = if (goal > 0) (total.toDouble() / goal).coerceIn(0.0, 1.0).toFloat() else 0f
        val activity = business?.tipoActividad.orEmpty().replaceFirstChar { it.uppercase() }
        _uiState.value = state.copy(
            products = visible.map { product ->
                val margin = product.unitCost?.let { cost -> if (product.price > 0) "${((product.price - cost).toDouble() / product.price * 100).toInt()}%" else "—" } ?: "—"
                ProductItem(product.id, product.name, when { product.isAsset -> "Activo"; product.isProduct -> "Producto"; else -> "Servicio" },
                    formatClp(product.price), product.unitCost?.let(::formatClp) ?: "—", margin,
                    if (product.trackStock) "${product.stock} ${product.unit}" else "—", product.active)
            },
            isLoading = loading, errorMessage = loadError, businessName = if (state.isEditingBusiness) state.businessName else business?.nombreNegocio.orEmpty(),
            businessDetails = listOf(business?.rubro, business?.region, business?.comuna).filter { !it.isNullOrBlank() }.joinToString(" · "),
            productCount = allProducts.count { it.isProduct && !it.isAsset }.toString(),
            serviceCount = allProducts.count { !it.isProduct && !it.isAsset }.toString(),
            assetCount = allProducts.count { it.isAsset }.toString(), goalTotal = formatClp(goal),
            goalProgressText = "${formatClp(total)} alcanzados este mes (${(percentage * 100).toInt()}%)", goalPercentage = percentage,
            rubro = if (state.isEditingBusiness) state.rubro else business?.rubro.orEmpty(), actividad = activity,
            region = if (state.isEditingBusiness) state.region else business?.region.orEmpty(),
            comuna = if (state.isEditingBusiness) state.comuna else business?.comuna.orEmpty(),
            registro = if (business != null) "Configurado" else ""
        )
    }
}
