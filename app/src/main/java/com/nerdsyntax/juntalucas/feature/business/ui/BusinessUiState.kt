package com.nerdsyntax.juntalucas.feature.business.ui

enum class BusinessTab { PRODUCTOS, INFORMACION }

data class ProductItem(
    val id: String,
    val name: String,
    val type: String, // Producto servicio o activo
    val price: String,
    val cost: String,
    val margin: String,
    val stock: String,
    val active: Boolean = true
)

data class BusinessUiState(
    val selectedTab: BusinessTab = BusinessTab.PRODUCTOS,
    val searchQuery: String = "",
    val selectedFilter: String = "Todos",
    val products: List<ProductItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isEditingBusiness: Boolean = false,
    val isSavingBusiness: Boolean = false,

    //Datos genericos para que veas donde va cada cosa
    val businessName: String = "",
    val businessDetails: String = "",
    val productCount: String = "0",
    val serviceCount: String = "0",
    val assetCount: String = "0",

    val goalTotal: String = "$0",
    val goalProgressText: String = "$0 alcanzados este mes (0%)",
    val goalPercentage: Float = 0f,

    val rubro: String = "",
    val actividad: String = "",
    val region: String = "",
    val comuna: String = "",
    val moneda: String = "Peso chileno (CLP)",
    val registro: String = ""
)
