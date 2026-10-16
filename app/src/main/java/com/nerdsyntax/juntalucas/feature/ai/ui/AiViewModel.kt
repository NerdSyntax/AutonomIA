package com.nerdsyntax.juntalucas.feature.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class AiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AiUiState())
    val uiState = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.8-flash",
        apiKey = "ponerapikeyaca"
    )

    init {
        cargarDatosDeFirebase()
    }


    private fun cargarDatosDeFirebase() {
        viewModelScope.launch {
            val fechaActual = LocalDate.now()
            val fechaAnterior = fechaActual.minusMonths(1)

            val mesActualStr = formatearFecha(fechaActual)
            val mesAnteriorStr = formatearFecha(fechaAnterior)

            try {
                delay(1500)

                _uiState.update {
                    it.copy(
                        isLoadingData = false,
                        periodos = listOf(mesActualStr, mesAnteriorStr),
                        periodoSeleccionado = mesActualStr,
                        periodoComparacion = mesAnteriorStr,
                        ventasRegistradas = 31,
                        gastosRegistrados = 18,
                        productosCatalogo = 4,
                        serviciosCatalogo = 2,
                        tieneDatosSuficientes = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingData = false, tieneDatosSuficientes = false) }
            }
        }
    }

    private fun formatearFecha(fecha: LocalDate): String {
        val mes = fecha.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            .replaceFirstChar { it.uppercase() }
        val anio = fecha.year
        return "$mes $anio"
    }


    fun onPeriodoSelected(periodo: String) {
        _uiState.update { it.copy(periodoSeleccionado = periodo) }
    }

    fun onAnalizarClick(onNavigateToLoading: () -> Unit) {
        if (!_uiState.value.tieneDatosSuficientes) {
            _uiState.update { it.copy(mostrarAdvertencia = true) }
        } else {
            _uiState.update { it.copy(isAnalyzing = true, pasoCargaActual = 0) }
            onNavigateToLoading()
        }
    }

    fun onOcultarAdvertencia() {
        _uiState.update { it.copy(mostrarAdvertencia = false) }
    }

    fun onCancelarAnalisis() {
        _uiState.update { it.copy(isAnalyzing = false) }
    }


    fun iniciarAnalisis(onTerminoAnalisis: () -> Unit) {
        viewModelScope.launch {
            try {
                delay(800)
                _uiState.update { it.copy(pasoCargaActual = 1) }
                delay(800)
                _uiState.update { it.copy(pasoCargaActual = 2) }

                val ventas = _uiState.value.ventasRegistradas
                val gastos = _uiState.value.gastosRegistrados

                val promptInicial = """
                    Eres el analista financiero de la app AutonomIA.
                    Datos de este mes del usuario: Ventas = $ventas, Gastos = $gastos.
                    Alerta detectada: El gasto en insumos aumentó un 19%.
                    
                    Escribe un ÚNICO mensaje de bienvenida. Saluda, menciona el hallazgo 
                    sobre los insumos y pregúntale cómo quiere proceder. Sé directo, profesional 
                    y amigable. No uses negritas excesivas ni formatos extraños.
                """.trimIndent()

                val response = generativeModel.generateContent(promptInicial)

                val mensajeInicial = ChatMessage(
                    text = response.text ?: "Análisis completado. ¿En qué te puedo ayudar hoy?",
                    isFromUser = false
                )

                _uiState.update {
                    it.copy(
                        chatMessages = listOf(mensajeInicial),
                        isAnalyzing = false
                    )
                }

                onTerminoAnalisis()

            } catch (e: Exception) {
                e.printStackTrace()

                val textoError = if (e.message?.contains("503") == true || e.message?.contains("high demand") == true) {
                    "Mis servidores están experimentando una alta demanda en este momento. Por favor, intenta analizar tu negocio en unos minutos."
                } else {
                    "Lo siento, tuve un problema de conexión al iniciar el análisis. Por favor, revisa tu conexión a internet e inténtalo de nuevo."
                }

                val errorMsg = ChatMessage(text = textoError, isFromUser = false)
                _uiState.update { it.copy(chatMessages = listOf(errorMsg), isAnalyzing = false) }
                onTerminoAnalisis()
            }
        }
    }

    fun onSendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(text = userText, isFromUser = true)
        _uiState.update { it.copy(chatMessages = it.chatMessages + userMessage, isTyping = true) }

        viewModelScope.launch {
            try {
                val ventas = _uiState.value.ventasRegistradas
                val gastos = _uiState.value.gastosRegistrados

                val promptSecreto = """
                    Eres analista financiero de AutonomIA.
                    Contexto del negocio: Ventas = $ventas, Gastos = $gastos.
                    Responde a la siguiente consulta del usuario de forma útil y profesional:
                    "$userText"
                """.trimIndent()

                val response = generativeModel.generateContent(promptSecreto)
                val iaMessage = ChatMessage(text = response.text ?: "No pude procesar tu solicitud.", isFromUser = false)

                _uiState.update { it.copy(chatMessages = it.chatMessages + iaMessage, isTyping = false) }
            } catch (e: Exception) {
                e.printStackTrace()

                val textoError = if (e.message?.contains("503") == true || e.message?.contains("high demand") == true) {
                    "Actualmente estoy procesando un volumen inusual de consultas. Por favor, espera un momento antes de enviarme otro mensaje."
                } else {
                    "Ocurrió un problema de red al intentar responderte. Verifica tu conexión, por favor."
                }

                val errorMsg = ChatMessage(text = textoError, isFromUser = false)
                _uiState.update { it.copy(chatMessages = it.chatMessages + errorMsg, isTyping = false) }
            }
            }
        }
    }
