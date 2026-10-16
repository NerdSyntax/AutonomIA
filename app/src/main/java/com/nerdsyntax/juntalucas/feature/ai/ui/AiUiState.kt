package com.nerdsyntax.juntalucas.feature.ai.ui

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean
)

data class AiUiState(
    val isLoadingData: Boolean = true,

    val periodos: List<String> = emptyList(), // Soluciona el Unresolved reference 'periodos'
    val periodoSeleccionado: String = "",
    val periodoComparacion: String = "",
    val ventasRegistradas: Int = 0,
    val gastosRegistrados: Int = 0,
    val productosCatalogo: Int = 0,
    val serviciosCatalogo: Int = 0,

    val tieneDatosSuficientes: Boolean = false,
    val mostrarAdvertencia: Boolean = false,
    val isAnalyzing: Boolean = false,
    val pasoCargaActual: Int = 0,

    val chatMessages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false
)