package com.nerdsyntax.juntalucas.feature.ai.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
private val DarkBlue = Color(0xFF0F2A4A)
private val HeaderGradientEnd = Color(0xFF1E3A5F)
private val GradientPurpleStart = Color(0xFF6366F1)
private val GradientPurpleEnd = Color(0xFF9333EA)
private val BrandPurple = Color(0xFF7C3AED)
private val LightPurple = Color(0xFFF3E8FF)
private val SoftIndigoBg = Color(0xFFEEF2FF)

private val GreenSuccess = Color(0xFF15803D)
private val GreenDarkBtn = Color(0xFF166534)
private val GreenSoftBg = Color(0xFFF0FDF4)

private val RedDanger = Color(0xFFB91C1C)
private val RedSoftBg = Color(0xFFFEF2F2)

private val AmberWarn = Color(0xFFF59E0B)
private val AmberText = Color(0xFFB45309)
private val AmberSoftBg = Color(0xFFFFFBEB)

private val BlueBadge = Color(0xFF2563EB)
private val BgGray = Color(0xFFF8FAFC)
private val PrivacyBg = Color(0xFFEFF6FF)
private val TextGray = Color(0xFF475569)
private val BorderLight = Color(0xFFE2E8F0)

private val AiPurpleGradient = Brush.horizontalGradient(
    colors = listOf(GradientPurpleStart, GradientPurpleEnd)
)

@Composable
fun AiScreen(
    state: AiUiState,
    vm: AiViewModel,
) {
    var mostrarComoFunciona by remember { mutableStateOf(false) }

    if (mostrarComoFunciona) {
        ComoFuncionaAnalisisDialog(
            onDismiss = { mostrarComoFunciona = false }
        )
    }

    if (state.isAnalyzing) {
        BackHandler(enabled = true) {}
        AiLoadingScreen(state = state)
        return
    }

    when (state.subScreen) {
        AiSubScreen.CHATBOX -> {
            BackHandler { vm.abrirPantallaPrincipal() }
            AiEmbeddedChatView(
                state = state,
                onBack = { vm.abrirPantallaPrincipal() },
                onSendMessage = { texto -> vm.onSendMessage(texto) }
            )
        }

        AiSubScreen.ANALYSIS_RESULT -> {
            BackHandler { vm.abrirPantallaPrincipal() }
            val report = state.currentReport
            if (report != null) {
                AiAnalysisResultView(
                    report = report,
                    onBack = { vm.abrirPantallaPrincipal() },
                    onDismissRecommendation = { vm.onDescartarRecomendacion(it) },
                    onOpenHistory = { vm.abrirHistorialAnalisis() }
                )
            } else {
                vm.abrirPantallaPrincipal()
            }
        }

        AiSubScreen.ANALYSIS_HISTORY -> {
            BackHandler { vm.abrirPantallaPrincipal() }
            AiAnalysisHistoryView(
                state = state,
                onBack = { vm.abrirPantallaPrincipal() },
                onSelectFilter = { vm.onHistoryFilterSelected(it) },
                onViewFullAnalysis = { vm.verAnalisisDesdeHistorial(it) }
            )
        }

        AiSubScreen.HOME -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgGray)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = DarkBlue,
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(top = 48.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "INTELIGENCIA ARTIFICIAL",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Consultoría Inteligente",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AiPurpleGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(LightPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✨", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Nuestro agente financiero evaluará los indicadores clave de tu negocio e identificará ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = DarkBlue)) {
                                        append("oportunidades estratégicas de mejora.")
                                    }
                                },
                                color = TextGray,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Periodo a evaluar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkBlue
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.periodos.isEmpty()) {
                            Text(
                                text = "Cargando periodos...",
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            state.periodos.forEach { periodo ->
                                val isSelected = periodo == state.periodoSeleccionado
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) LightPurple else Color.White,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) BrandPurple else BorderLight
                                    ),
                                    shadowElevation = if (isSelected) 0.dp else 1.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { vm.onPeriodoSelected(periodo) }
                                ) {
                                    Text(
                                        text = periodo,
                                        color = if (isSelected) BrandPurple else TextGray,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Métricas disponibles para el análisis",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DarkBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                KpiStatPill(
                                    modifier = Modifier.weight(1f),
                                    label = "Ventas registradas",
                                    value = state.ventasRegistradas.toString(),
                                    valueColor = GreenSuccess,
                                    bgColor = GreenSoftBg
                                )
                                KpiStatPill(
                                    modifier = Modifier.weight(1f),
                                    label = "Gastos registrados",
                                    value = state.gastosRegistrados.toString(),
                                    valueColor = RedDanger,
                                    bgColor = RedSoftBg
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                KpiStatPill(
                                    modifier = Modifier.weight(1f),
                                    label = "Productos activos",
                                    value = state.productosCatalogo.toString(),
                                    valueColor = DarkBlue,
                                    bgColor = BgGray
                                )
                                KpiStatPill(
                                    modifier = Modifier.weight(1f),
                                    label = "Servicios activos",
                                    value = state.serviciosCatalogo.toString(),
                                    valueColor = DarkBlue,
                                    bgColor = BgGray
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 14.dp),
                                color = Color(0xFFF1F5F9)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Base comparativa", color = TextGray, fontSize = 13.sp)
                                if (state.periodoComparacion.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SoftIndigoBg
                                    ) {
                                        Text(
                                            text = state.periodoComparacion,
                                            color = BlueBadge,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                } else {
                                    Text("Sin periodo previo", color = TextGray, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PrivacyBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = BlueBadge,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Procesamiento confidencial y seguro",
                                    color = DarkBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Se evalúan exclusivamente métricas agregadas sin exponer datos sensibles de tus clientes.",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        onClick = { vm.onGenerarAnalisisEstructuradoClick() },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .background(AiPurpleGradient)
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Generar Diagnóstico Ejecutivo (3 claves)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        onClick = { vm.abrirChatboxDirecto() },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, GradientPurpleStart.copy(alpha = 0.4f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(LightPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = BrandPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Asistente Financiero IA",
                                    color = DarkBlue,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Consulta interactiva sobre métricas y rentabilidad",
                                    color = TextGray,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = BrandPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¿Cómo funciona nuestra inteligencia artificial?",
                            color = BrandPurple,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { mostrarComoFunciona = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { vm.abrirHistorialAnalisis() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = TextGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.historialAnalisis.isNotEmpty()) {
                                    "Historial de informes (${state.historialAnalisis.size})"
                                } else {
                                    "Historial de informes"
                                },
                                color = TextGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}

@Composable
private fun AiEmbeddedChatView(
    state: AiUiState,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val sugerenciasRapidas = listOf(
        "¿En qué categoría concentré mis mayores gastos?",
        "¿Qué producto me dejó mayor margen de ganancia?",
        "¿Cómo van mis ventas respecto a la meta mensual?",
        "¿Qué estrategia me sugieres para subir el ticket promedio?"
    )

    val mostrarMenuSugerencias = state.chatMessages.size <= 1 && !state.isTyping

    LaunchedEffect(state.chatMessages.size, state.isTyping) {
        if (state.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.chatMessages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .imePadding()
    ) {
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, bottom = 14.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = BgGray,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Volver",
                                tint = DarkBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Chat de mi negocio",
                            color = DarkBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Consultoría financiera asistida por IA",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AiPurpleGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "IA",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Respuestas basadas en ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DarkBlue)) {
                                append(state.periodoSeleccionado.ifBlank { "Periodo actual" })
                            }
                        },
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.chatMessages, key = { it.id }) { msg ->
                if (msg.isFromUser) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 4.dp
                            ),
                            color = DarkBlue,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AiPurpleGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 18.dp
                            ),
                            color = Color.White,
                            shadowElevation = 1.dp,
                            border = BorderStroke(0.5.dp, BorderLight),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = formatAiStructuredMessage(msg.text),
                                    color = DarkBlue,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Datos sincronizados · ${state.periodoSeleccionado.ifBlank { "Periodo actual" }}",
                                    color = TextGray.copy(alpha = 0.65f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
            if (mostrarMenuSugerencias) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 44.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "PREGUNTAS SUGERIDAS",
                            color = TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )

                        sugerenciasRapidas.forEach { sugerencia ->
                            OutlinedCard(
                                onClick = { onSendMessage(sugerencia) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BorderLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sugerencia,
                                        color = DarkBlue,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = TextGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.isTyping) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AiPurpleGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 1.dp,
                            border = BorderStroke(0.5.dp, BorderLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(15.dp),
                                    strokeWidth = 2.dp,
                                    color = BrandPurple
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Procesando métricas...",
                                    color = TextGray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgGray)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 84.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Escribe una consulta sobre tu negocio...",
                                color = TextGray.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    val textoEnviar = inputText
                                    inputText = ""
                                    focusManager.clearFocus()
                                    onSendMessage(textoEnviar)
                                }
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = BrandPurple
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    val isSendEnabled = inputText.isNotBlank()
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSendEnabled) AiPurpleGradient
                                else Brush.horizontalGradient(listOf(BorderLight, BorderLight))
                            )
                            .clickable(enabled = isSendEnabled) {
                                val textoEnviar = inputText
                                inputText = ""
                                focusManager.clearFocus()
                                onSendMessage(textoEnviar)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (isSendEnabled) Color.White else TextGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun AiAnalysisResultView(
    report: AiStructuredReport,
    onBack: () -> Unit,
    onDismissRecommendation: (String) -> Unit,
    onOpenHistory: () -> Unit
) {
    val context = LocalContext.current
    var recomendacionDetalle by remember { mutableStateOf<AiRecommendation?>(null) }
    if (recomendacionDetalle != null) {
        val rec = recomendacionDetalle!!
        Dialog(onDismissRequest = { recomendacionDetalle = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AiPurpleGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${rec.number}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = rec.title,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue,
                                fontSize = 17.sp,
                                lineHeight = 22.sp
                            )
                        }
                        IconButton(
                            onClick = { recomendacionDetalle = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = rec.description,
                        color = TextGray,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )

                    if (rec.detailExplanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BgGray,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = rec.detailExplanation,
                                color = DarkBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = LightPurple,
                        border = BorderStroke(1.dp, BrandPurple.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✨", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Plan de acción sugerido",
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPurple,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = rec.suggestedAction,
                                color = DarkBlue,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Surface(
                        onClick = { recomendacionDetalle = null },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .background(AiPurpleGradient)
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Entendido",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DarkBlue,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(top = 48.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "INFORME EJECUTIVO",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Resultado del análisis",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = report.periodo.ifBlank { "Actual" },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            val bannerBg = if (report.requiereAtencion) AmberSoftBg else GreenSoftBg
            val bannerAccent = if (report.requiereAtencion) AmberWarn else GreenSuccess
            val bannerTitleColor = if (report.requiereAtencion) AmberText else GreenDarkBtn
            val statusEmoji = if (report.requiereAtencion) "⚠️" else "✅"

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, bannerAccent.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerBg.copy(alpha = 0.55f))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = bannerAccent.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(statusEmoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = report.estadoTitulo.uppercase(),
                                    color = bannerTitleColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = report.estadoDescripcion,
                        color = DarkBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 21.sp
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resumen de indicadores clave",
                            color = DarkBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IndicatorMiniCard(
                            modifier = Modifier.weight(1f),
                            label = "Ventas totales",
                            value = report.ventas,
                            diffText = report.ventasDiff,
                            isPositive = report.ventasDiffPositivo,
                            cardBgColor = GreenSoftBg,
                            valueColor = GreenSuccess
                        )
                        IndicatorMiniCard(
                            modifier = Modifier.weight(1f),
                            label = "Gastos totales",
                            value = report.gastos,
                            diffText = report.gastosDiff,
                            isPositive = report.gastosDiffPositivo,
                            cardBgColor = RedSoftBg,
                            valueColor = RedDanger
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IndicatorMiniCard(
                            modifier = Modifier.weight(1f),
                            label = "Ganancia bruta",
                            value = report.ganancia,
                            diffText = report.gananciaDiff,
                            isPositive = report.gananciaDiffPositivo,
                            cardBgColor = SoftIndigoBg,
                            valueColor = BlueBadge
                        )
                        IndicatorMiniCard(
                            modifier = Modifier.weight(1f),
                            label = "Margen bruto",
                            value = report.margen,
                            diffText = report.margenDiff,
                            isPositive = report.margenDiffPositivo,
                            cardBgColor = LightPurple.copy(alpha = 0.6f),
                            valueColor = BrandPurple
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯", fontSize = 17.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3 recomendaciones prioritarias",
                        color = DarkBlue,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val recomendacionesVisibles = report.recomendaciones.filter { !it.isDismissed }
            if (recomendacionesVisibles.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¡Todo al día!",
                            color = DarkBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Has revisado todas las recomendaciones estratégicas de este periodo.",
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                recomendacionesVisibles.forEach { rec ->
                    PriorityRecommendationCard(
                        recommendation = rec,
                        onDetailClick = { recomendacionDetalle = rec },
                        onDismissClick = { onDismissRecommendation(rec.id) }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = onOpenHistory,
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .weight(1.15f)
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AiPurpleGradient)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seguimiento",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    onClick = {
                        val shareText = buildString {
                            appendLine("📊 Diagnóstico Ejecutivo AutonomIA (${report.periodo})")
                            appendLine("Estado: ${report.estadoTitulo}")
                            appendLine("Ventas: ${report.ventas} | Gastos: ${report.gastos}")
                            appendLine("Ganancia: ${report.ganancia} (Margen: ${report.margen})")
                            appendLine()
                            appendLine("Recomendaciones clave:")
                            report.recomendaciones.forEach { r ->
                                appendLine("${r.number}. ${r.title}: ${r.suggestedAction}")
                            }
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir informe"))
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = null,
                            tint = DarkBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Compartir",
                            color = DarkBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}
@Composable
private fun AiAnalysisHistoryView(
    state: AiUiState,
    onBack: () -> Unit,
    onSelectFilter: (HistoryFilter) -> Unit,
    onViewFullAnalysis: (AnalysisHistoryItem) -> Unit
) {
    val listaFiltrada = remember(state.historialAnalisis, state.historyFilter) {
        when (state.historyFilter) {
            HistoryFilter.ALL -> state.historialAnalisis
            HistoryFilter.STABLE -> state.historialAnalisis.filter { !it.requiereAtencion }
            HistoryFilter.ATTENTION -> state.historialAnalisis.filter { it.requiereAtencion }
        }
    }

    val totalRealizados = state.historialAnalisis.size
    val totalRecomendaciones = state.historialAnalisis
        .sumOf { it.totalCount }
        .coerceAtLeast(if (totalRealizados > 0) totalRealizados * 3 else 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DarkBlue,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(top = 48.dp, bottom = 22.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "REGISTRO EJECUTIVO",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Historial de informes",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AiPurpleGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryFilterChip(
                    label = "Todos",
                    selected = state.historyFilter == HistoryFilter.ALL,
                    onClick = { onSelectFilter(HistoryFilter.ALL) }
                )
                HistoryFilterChip(
                    label = "Estables",
                    selected = state.historyFilter == HistoryFilter.STABLE,
                    onClick = { onSelectFilter(HistoryFilter.STABLE) }
                )
                HistoryFilterChip(
                    label = "Con alertas",
                    selected = state.historyFilter == HistoryFilter.ATTENTION,
                    onClick = { onSelectFilter(HistoryFilter.ATTENTION) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftIndigoBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📊", fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = totalRealizados.toString(),
                            color = DarkBlue,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Informes generados",
                            color = TextGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(LightPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = totalRecomendaciones.toString(),
                            color = BrandPurple,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Acciones sugeridas",
                            color = TextGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (listaFiltrada.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(LightPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Analytics,
                                contentDescription = null,
                                tint = BrandPurple,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Sin informes en esta categoría",
                            color = DarkBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Genera un Diagnóstico Ejecutivo desde la pantalla principal para consultar tu historial aquí.",
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                    }
                }
            } else {
                listaFiltrada.forEach { item ->
                    HistoryMonthCard(
                        item = item,
                        onViewComplete = { onViewFullAnalysis(item) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}
@Composable
private fun KpiStatPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = label,
                color = TextGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun IndicatorMiniCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    diffText: String,
    isPositive: Boolean,
    cardBgColor: Color = BgGray,
    valueColor: Color = DarkBlue
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBgColor,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                color = Color(0xFF475569),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.White.copy(alpha = 0.75f)
            ) {
                Text(
                    text = diffText,
                    color = if (isPositive) GreenSuccess else RedDanger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PriorityRecommendationCard(
    recommendation: AiRecommendation,
    onDetailClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    val accentColor = when (recommendation.priority) {
        RecommendationPriority.HIGH -> RedDanger
        RecommendationPriority.MEDIUM -> AmberWarn
        RecommendationPriority.LOW -> GreenDarkBtn
    }
    val softBgColor = when (recommendation.priority) {
        RecommendationPriority.HIGH -> RedSoftBg
        RecommendationPriority.MEDIUM -> AmberSoftBg
        RecommendationPriority.LOW -> GreenSoftBg
    }
    val actionTitleColor = when (recommendation.priority) {
        RecommendationPriority.HIGH -> RedDanger
        RecommendationPriority.MEDIUM -> AmberText
        RecommendationPriority.LOW -> GreenDarkBtn
    }
    val priorityLabel = when (recommendation.priority) {
        RecommendationPriority.HIGH -> "Prioridad Alta"
        RecommendationPriority.MEDIUM -> "Prioridad Media"
        RecommendationPriority.LOW -> "Oportunidad"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(0.5.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(accentColor)
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(softBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${recommendation.number}",
                                color = actionTitleColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = softBgColor
                        ) {
                            Text(
                                text = priorityLabel,
                                color = actionTitleColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Surface(
                        onClick = onDismissClick,
                        shape = CircleShape,
                        color = BgGray,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Descartar",
                                tint = TextGray,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = recommendation.title,
                    color = DarkBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = recommendation.description,
                    color = TextGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = softBgColor.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💡", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Acción recomendada",
                                color = actionTitleColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recommendation.suggestedAction,
                            color = DarkBlue,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    onClick = onDetailClick,
                    shape = RoundedCornerShape(14.dp),
                    color = BgGray,
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Explorar análisis detallado",
                            color = DarkBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = DarkBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.12f),
        modifier = Modifier.height(34.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = label,
                color = if (selected) DarkBlue else Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HistoryMonthCard(
    item: AnalysisHistoryItem,
    onViewComplete: () -> Unit
) {
    val accentColor = if (item.requiereAtencion) AmberWarn else GreenSuccess
    val badgeBg = if (item.requiereAtencion) AmberSoftBg else GreenSoftBg
    val badgeText = if (item.requiereAtencion) "Requiere atención" else "Negocio estable"
    val badgeTextColor = if (item.requiereAtencion) AmberText else GreenDarkBtn
    val progreso = (item.totalCount.toFloat() / 3f).coerceIn(0.33f, 1f)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(0.5.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(accentColor)
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = item.periodo.ifBlank { "Periodo analizado" },
                            color = DarkBlue,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Generado el ${item.fechaHora}",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeBg
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = badgeText,
                                color = badgeTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GreenSoftBg.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Balance del periodo", color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.balance,
                                color = GreenDarkBtn,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = LightPurple.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Plan de acción", color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.totalCount} claves",
                                color = BrandPurple,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(BorderLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progreso)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(AiPurpleGradient)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    onClick = onViewComplete,
                    shape = RoundedCornerShape(14.dp),
                    color = LightPurple,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Revisar informe ejecutivo",
                            color = BrandPurple,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = BrandPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComoFuncionaAnalisisDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AiPurpleGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "¿Cómo funciona?",
                            color = DarkBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                PasoExplicacionItem(
                    icono = Icons.Outlined.Sync,
                    titulo = "1. Sincronización de métricas",
                    descripcion = "Consolidamos tus ventas, estructura de costos y catálogo registrados en el periodo seleccionado."
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasoExplicacionItem(
                    icono = Icons.Outlined.Analytics,
                    titulo = "2. Diagnóstico Ejecutivo",
                    descripcion = "Identificamos variaciones críticas y generamos 3 recomendaciones accionables ordenadas por impacto."
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasoExplicacionItem(
                    icono = Icons.Outlined.Lightbulb,
                    titulo = "3. Asistente Financiero IA",
                    descripcion = "En el chat interactivo puedes realizar consultas puntuales sobre márgenes, insumos y proyecciones."
                )

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PrivacyBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = BlueBadge,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tus datos se procesan de forma anónima y segura únicamente para generar tus informes.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(AiPurpleGradient)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Entendido",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasoExplicacionItem(
    icono: ImageVector,
    titulo: String,
    descripcion: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LightPurple),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = BrandPurple,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = DarkBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = descripcion,
                color = TextGray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
private fun formatAiStructuredMessage(rawText: String): AnnotatedString {
    return buildAnnotatedString {
        val cleanText = rawText.replace("**", "")
        val lines = cleanText.split("\n")
        val highlightHeaders = listOf(
            "Hecho observado",
            "Recomendación",
            "Acción sugerida",
            "Conclusión",
            "Análisis",
            "Diagnóstico"
        )

        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            val matchedHeader = highlightHeaders.firstOrNull { header ->
                trimmed.startsWith(header, ignoreCase = true)
            }

            if (matchedHeader != null) {
                val colonIndex = trimmed.indexOf(':')
                if (colonIndex != -1) {
                    val titlePart = trimmed.substring(0, colonIndex + 1)
                    val restPart = trimmed.substring(colonIndex + 1)
                    withStyle(SpanStyle(color = BrandPurple, fontWeight = FontWeight.Bold)) {
                        append(titlePart)
                    }
                    append(restPart)
                } else {
                    withStyle(SpanStyle(color = BrandPurple, fontWeight = FontWeight.Bold)) {
                        append(trimmed)
                    }
                }
            } else {
                append(line)
            }

            if (index < lines.lastIndex) {
                append("\n")
            }
        }
    }
}