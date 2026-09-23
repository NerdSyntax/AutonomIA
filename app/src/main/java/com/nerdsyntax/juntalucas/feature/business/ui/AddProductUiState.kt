package com.nerdsyntax.juntalucas.feature.business.ui

data class AddProductUiState(
    val isProduct: Boolean = true,
    val nombre: String = "",
    val categoria: String = "Repostería",
    val precio: String = "",
    val costo: String = "",
    val descripcion: String = "",
    val stockActual: String = "0",
    val stockMinimo: String = "2",
    val unidad: String = "unidad",
    val isActive: Boolean = true
) {
    val precioNum: Double get() = precio.replace(".", "").toDoubleOrNull() ?: 0.0
    val costoNum: Double get() = costo.replace(".", "").toDoubleOrNull() ?: 0.0
    val ganancia: Double get() = precioNum - costoNum
    val margen: Double get() = if (precioNum > 0) (ganancia / precioNum) * 100 else 0.0
    val isProfitable: Boolean get() = ganancia >= 0
}