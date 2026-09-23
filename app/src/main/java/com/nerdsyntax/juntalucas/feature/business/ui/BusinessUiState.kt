package com.nerdsyntax.juntalucas.feature.business.ui

enum class BusinessTab { PRODUCTOS, INFORMACION }

data class ProductItem(
    val id: String,
    val name: String,
    val type: String, // Producto servicio o activo
    val price: String,
    val cost: String,
    val margin: String,
    val stock: String
)

data class BusinessUiState(
    val selectedTab: BusinessTab = BusinessTab.PRODUCTOS,
    val searchQuery: String = "",
    val selectedFilter: String = "Todos",
    val products: List<ProductItem> = emptyList(),

    //Datos genericos para que veas donde va cada cosa
    val businessName: String = "Nombre de negocio",
    val businessDetails: String = "Rubro de ejemplo · Región, Comuna",
    val productCount: String = "4",
    val serviceCount: String = "2",
    val assetCount: String = "5",

    val goalTotal: String = "$2.000.000",
    val goalProgressText: String = "$1.800.000 alcanzados este mes (90%)",
    val goalPercentage: Float = 0.9f,

    val rubro: String = "Rubro de ejemplo",
    val actividad: String = "Productos y servicios",
    val region: String = "Región de ejemplo",
    val comuna: String = "Comuna de ejemplo",
    val moneda: String = "Peso chileno (CLP)",
    val registro: String = "Septiembre 2026"
)