package com.nerdsyntax.juntalucas.feature.ai.ui

data class AiUiState(
    val tieneDatosSuficientes: Boolean = false,
    val mostrarAdvertencia: Boolean = false,
    val periodos: List<String> = emptyList(),
    val periodoSeleccionado: String = "",
    val ventasRegistradas: Int = 0,
    val gastosRegistrados: Int = 0,
    val productosCatalogo: Int = 0,
    val serviciosCatalogo: Int = 0,
    val periodoComparacion: String = ""
)