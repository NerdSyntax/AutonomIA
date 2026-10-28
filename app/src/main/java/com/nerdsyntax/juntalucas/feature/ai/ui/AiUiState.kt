package com.nerdsyntax.juntalucas.feature.ai.ui

import java.util.UUID

enum class AiSubScreen {
    HOME,
    ANALYSIS_RESULT,
    ANALYSIS_HISTORY,
    CHATBOX
}

enum class HistoryFilter {
    ALL,
    STABLE,
    ATTENTION
}

enum class RecommendationPriority {
    HIGH,
    MEDIUM,
    LOW
}

data class AiRecommendation(
    val id: String = UUID.randomUUID().toString(),
    val number: Int,
    val title: String,
    val description: String,
    val suggestedAction: String,
    val detailExplanation: String = "",
    val priority: RecommendationPriority,
    val isApplied: Boolean = false,
    val isDismissed: Boolean = false
)

data class AiStructuredReport(
    val id: String = UUID.randomUUID().toString(),
    val periodo: String,
    val fechaAnalizado: String,
    val requiereAtencion: Boolean,
    val estadoTitulo: String,
    val estadoDescripcion: String,
    val ventas: String,
    val ventasDiff: String,
    val ventasDiffPositivo: Boolean = true,
    val gastos: String,
    val gastosDiff: String,
    val gastosDiffPositivo: Boolean = false,
    val ganancia: String,
    val gananciaValor: Long = 0L,
    val gananciaDiff: String,
    val gananciaDiffPositivo: Boolean = true,
    val margen: String,
    val margenDiff: String,
    val margenDiffPositivo: Boolean = true,
    val recomendaciones: List<AiRecommendation> = emptyList()
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean
)

data class AnalysisHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val fechaHora: String,
    val periodo: String,
    val resumen: String,
    val ventasTotal: String,
    val gastosTotal: String,
    val balance: String,
    val requiereAtencion: Boolean = false,
    val aplicadasCount: Int = 0,
    val totalCount: Int = 3,
    val report: AiStructuredReport? = null
)

data class AiUiState(
    val isLoadingData: Boolean = true,
    val subScreen: AiSubScreen = AiSubScreen.HOME,
    val historyFilter: HistoryFilter = HistoryFilter.ALL,
    val periodos: List<String> = emptyList(),
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
    val currentReport: AiStructuredReport? = null,
    val historialAnalisis: List<AnalysisHistoryItem> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false
)