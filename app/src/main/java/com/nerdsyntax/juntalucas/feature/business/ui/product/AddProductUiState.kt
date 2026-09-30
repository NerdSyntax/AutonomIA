package com.nerdsyntax.juntalucas.feature.business.ui.product

import com.nerdsyntax.juntalucas.core.format.parseClp

data class AddProductUiState(
    val isProduct: Boolean = true,
    val nombre: String = "",
    val categoria: String = "",
    val precio: String = "",
    val costo: String = "",
    val descripcion: String = "",
    val stockActual: String = "0",
    val stockMinimo: String = "2",
    val unidad: String = "unidad",
    val isActive: Boolean = true,
    val trackStock: Boolean = true,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val precioNum: Long get() = parseClp(precio) ?: 0L
    val costoNum: Long? get() = if (costo.isBlank()) null else parseClp(costo)
    val ganancia: Long get() = precioNum - (costoNum ?: 0L)
    val margen: Double get() = if (precioNum > 0) (ganancia.toDouble() / precioNum) * 100 else 0.0
    val isProfitable: Boolean get() = ganancia >= 0
}
