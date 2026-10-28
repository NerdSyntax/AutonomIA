package com.nerdsyntax.juntalucas.feature.ai.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Analytics
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
private val HeaderGradientEnd = Color(0xFF2E1A68)
private val BrandPurple = Color(0xFF6D28D9)
private val LightPurple = Color(0xFFEDE9FE)
private val GreenSuccess = Color(0xFF16A34A)
private val GreenDarkBtn = Color(0xFF2E7D32)
private val GreenSoftBg = Color(0xFFE8F5E9)
private val RedDanger = Color(0xFFB91C1C)
private val RedSoftBg = Color(0xFFFEE2E2)
private val AmberWarn = Color(0xFFF59E0B)
private val AmberText = Color(0xFFD97706)
private val AmberSoftBg = Color(0xFFFEF3C7)
private val BlueBadge = Color(0xFF3B82F6)
private val BgGray = Color(0xFFF8FAFC)
private val PrivacyBg = Color(0xFFEFF6FF)
private val TextGray = Color(0xFF475569)

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
                        .background(DarkBlue)
                        .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Análisis inteligente",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandPurple)
                        }
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("AutonomIA analizará los indicadores de tu negocio y te ayudará a identificar ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("qué deberías mejorar primero.")
                                }
                            },
                            color = TextGray,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Periodo a analizar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.periodos.isEmpty()) {
                            Text("Cargando periodos...", color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
                        } else {
                            state.periodos.forEach { periodo ->
                                val isSelected = periodo == state.periodoSeleccionado
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) LightPurple else Color.White,
                                    border = BorderStroke(1.dp, if (isSelected) BrandPurple else Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { vm.onPeriodoSelected(periodo) }
                                ) {
                                    Text(
                                        text = periodo,
                                        color = if (isSelected) BrandPurple else TextGray,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Datos disponibles para el análisis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                            Spacer(modifier = Modifier.height(16.dp))

                            StatRow("Ventas registradas", state.ventasRegistradas.toString(), GreenSuccess)
                            StatRow("Gastos registrados", state.gastosRegistrados.toString(), RedDanger)
                            StatRow("Productos en catálogo", state.productosCatalogo.toString(), DarkBlue)
                            StatRow("Servicios en catálogo", state.serviciosCatalogo.toString(), DarkBlue)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Periodo de comparación", color = TextGray, fontSize = 14.sp)
                                if (state.periodoComparacion.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = BlueBadge
                                    ) {
                                        Text(
                                            text = state.periodoComparacion,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Text("-", color = TextGray, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrivacyBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Outlined.Security, contentDescription = null, tint = DarkBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Tu privacidad está protegida", color = DarkBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Solo se analizarán datos generales de tu negocio. No se enviará información personal de clientes.", color = TextGray, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { vm.onGenerarAnalisisEstructuradoClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Análisis de IA (3 recomendaciones)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { vm.abrirChatboxDirecto() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, BrandPurple),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple)
                    ) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Consultar al Chatbox IA", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¿Cómo funciona este análisis?",
                            color = BrandPurple,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { mostrarComoFunciona = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { vm.abrirHistorialAnalisis() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.historialAnalisis.isNotEmpty()) {
                                    "Ver historial de análisis (${state.historialAnalisis.size})"
                                } else {
                                    "Ver historial de análisis"
                                },
                                color = TextGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
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
        "¿Qué productos nuevos me sugieres agregar al catálogo?",
        "¿Cuál es mi producto más rentable?",
        "¿Cómo reduzco mis gastos este mes?",
        "¿Qué opinas del rendimiento del negocio este mes?",
        "¿Cuánto debo vender por día?"
    )

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBlue)
                .padding(top = 48.dp, bottom = 18.dp, start = 16.dp, end = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AutonomIA Analista",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Analizando ${state.periodoSeleccionado}",
                        color = Color.White.copy(alpha = 0.7f),
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
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.chatMessages, key = { it.id }) { msg ->
                val alignment = if (msg.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
                val bubbleBg = if (msg.isFromUser) BrandPurple else Color.White
                val textColor = if (msg.isFromUser) Color.White else DarkBlue
                val shape = if (msg.isFromUser) {
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = alignment
                ) {
                    Surface(
                        shape = shape,
                        color = bubbleBg,
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            if (state.isTyping) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = BrandPurple
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Pensando...", color = TextGray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sugerenciasRapidas.forEach { sugerencia ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = LightPurple,
                    border = BorderStroke(1.dp, BrandPurple.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        onSendMessage(sugerencia)
                    }
                ) {
                    Text(
                        text = sugerencia,
                        color = BrandPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Pregúntale a AutonomIA...", color = TextGray.copy(alpha = 0.6f), fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(26.dp),
                    singleLine = false,
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenSuccess,
                        unfocusedBorderColor = GreenSuccess.copy(alpha = 0.6f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val textoEnviar = inputText
                            inputText = ""
                            focusManager.clearFocus()
                            onSendMessage(textoEnviar)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandPurple)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White
                    )
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
        AlertDialog(
            onDismissRequest = { recomendacionDetalle = null },
            title = {
                Text(
                    text = "${rec.number}. ${rec.title}",
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(rec.description, color = TextGray, fontSize = 14.sp)
                    if (rec.detailExplanation.isNotBlank()) {
                        Text(rec.detailExplanation, color = DarkBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = LightPurple,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Acción recomendada:", fontWeight = FontWeight.Bold, color = BrandPurple, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(rec.suggestedAction, color = DarkBlue, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { recomendacionDetalle = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) {
                    Text("Cerrar")
                }
            }
        )
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
                    Brush.horizontalGradient(
                        colors = listOf(DarkBlue, HeaderGradientEnd)
                    )
                )
                .padding(top = 48.dp, bottom = 22.dp, start = 16.dp, end = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Resultado del análisis",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            val bannerBg = if (report.requiereAtencion) AmberSoftBg else GreenSoftBg
            val bannerAccent = if (report.requiereAtencion) AmberWarn else GreenSuccess
            val bannerTitleColor = if (report.requiereAtencion) Color(0xFF92400E) else GreenDarkBtn

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = bannerBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(bannerAccent)
                    )
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(bannerAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = report.estadoTitulo,
                                color = bannerTitleColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = report.estadoDescripcion,
                            color = DarkBlue,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Indicadores del análisis",
                        color = DarkBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IndicatorMiniCard(
                            modifier = Modifier.weight(1f),
                            label = "Ventas",
                            value = report.ventas,
                            diffText = report.ventasDiff,
                            isPositive = report.ventasDiffPositivo
                        )
                        IndicatorMiniCard(
                            modifier = Modifier.weight(1f),
                            label = "Gastos",
                            value = report.gastos,
                            diffText = report.gastosDiff,
                            isPositive = report.gastosDiffPositivo
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IndicatorMiniCard(
                            modifier = Modifier.weight(1f),
                            label = "Ganancia",
                            value = report.ganancia,
                            diffText = report.gananciaDiff,
                            isPositive = report.gananciaDiffPositivo
                        )
                        IndicatorMiniCard(
                            modifier = Modifier.weight(1f),
                            label = "Margen",
                            value = report.margen,
                            diffText = report.margenDiff,
                            isPositive = report.margenDiffPositivo
                        )
                    }
                }
            }

            Text(
                text = "3 recomendaciones prioritarias",
                color = DarkBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            val recomendacionesVisibles = report.recomendaciones.filter { !it.isDismissed }
            if (recomendacionesVisibles.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Has revisado todas las recomendaciones de este periodo.",
                        color = TextGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = LightPurple,
                    border = BorderStroke(1.dp, BrandPurple.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1.15f)
                        .height(48.dp)
                        .clickable { onOpenHistory() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Seguimiento", color = BrandPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable {
                            val shareText = buildString {
                                appendLine("📊 Diagnóstico AutonomIA (${report.periodo})")
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
                            context.startActivity(Intent.createChooser(intent, "Compartir análisis"))
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", color = DarkBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun IndicatorMiniCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    diffText: String,
    isPositive: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BgGray,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, color = TextGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = DarkBlue,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = diffText,
                color = if (isPositive) GreenSuccess else RedDanger,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
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
        RecommendationPriority.HIGH -> RedSoftBg.copy(alpha = 0.65f)
        RecommendationPriority.MEDIUM -> AmberSoftBg.copy(alpha = 0.7f)
        RecommendationPriority.LOW -> GreenSoftBg.copy(alpha = 0.8f)
    }
    val actionTitleColor = when (recommendation.priority) {
        RecommendationPriority.HIGH -> RedDanger
        RecommendationPriority.MEDIUM -> AmberText
        RecommendationPriority.LOW -> GreenDarkBtn
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(softBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recommendation.number.toString(),
                            color = actionTitleColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = recommendation.title,
                        color = DarkBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = recommendation.description,
                    color = TextGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = softBgColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Acción sugerida",
                            color = actionTitleColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recommendation.suggestedAction,
                            color = DarkBlue,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDetailClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, accentColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = actionTitleColor)
                    ) {
                        Text("Ver detalle", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onDismissClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Descartar",
                                tint = TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
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
    val totalRecomendaciones = state.historialAnalisis.sumOf { it.totalCount }.coerceAtLeast(if (totalRealizados > 0) totalRealizados * 3 else 0)

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
                    Brush.horizontalGradient(
                        colors = listOf(DarkBlue, HeaderGradientEnd)
                    )
                )
                .padding(top = 48.dp, bottom = 22.dp, start = 16.dp, end = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Historial de análisis",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryFilterChip(
                    label = "Todos",
                    selected = state.historyFilter == HistoryFilter.ALL,
                    onClick = { onSelectFilter(HistoryFilter.ALL) }
                )
                HistoryFilterChip(
                    label = "Negocio estable",
                    selected = state.historyFilter == HistoryFilter.STABLE,
                    onClick = { onSelectFilter(HistoryFilter.STABLE) }
                )
                HistoryFilterChip(
                    label = "Requiere atención",
                    selected = state.historyFilter == HistoryFilter.ATTENTION,
                    onClick = { onSelectFilter(HistoryFilter.ATTENTION) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = totalRealizados.toString(),
                            color = DarkBlue,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Análisis realizados",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = totalRecomendaciones.toString(),
                            color = DarkBlue,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Recomendaciones generadas",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (listaFiltrada.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = TextGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aún no hay análisis en esta categoría",
                            color = DarkBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Usa el botón \"Análisis de IA\" para generar tu diagnóstico con recomendaciones.",
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
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
private fun HistoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) DarkBlue else Color.White,
        border = BorderStroke(1.dp, if (selected) DarkBlue else Color(0xFFE2E8F0)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextGray,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun HistoryMonthCard(
    item: AnalysisHistoryItem,
    onViewComplete: () -> Unit
) {
    val accentColor = if (item.requiereAtencion) AmberWarn else GreenDarkBtn
    val badgeBg = if (item.requiereAtencion) AmberSoftBg else GreenSoftBg
    val badgeText = if (item.requiereAtencion) "Requiere atención" else "Negocio estable"
    val badgeTextColor = if (item.requiereAtencion) AmberText else GreenDarkBtn
    val progreso = (item.totalCount.toFloat() / 3f).coerceIn(0.33f, 1f)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
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
                            text = item.periodo.ifBlank { "Octubre 2026" },
                            color = DarkBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Analizado el ${item.fechaHora}",
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
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
                        shape = RoundedCornerShape(12.dp),
                        color = BgGray,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Ganancia del periodo", color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.balance,
                                color = GreenDarkBtn,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BgGray,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Recomendaciones", color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${item.totalCount} prioritarias",
                                color = DarkBlue,
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
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progreso)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(accentColor)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onViewComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, accentColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = badgeTextColor)
                ) {
                    Text(
                        text = "Ver análisis completo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                                .background(LightPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BrandPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "¿Cómo funciona?",
                            color = DarkBlue,
                            fontSize = 19.sp,
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
                    titulo = "1. Recopilación en tiempo real",
                    descripcion = "Reunimos las ventas, gastos y productos que has registrado en el periodo seleccionado dentro de la aplicación."
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasoExplicacionItem(
                    icono = Icons.Outlined.Analytics,
                    titulo = "2. Diagnóstico estructurado",
                    descripcion = "Con el botón 'Análisis de IA' obtienes indicadores comparativos y 3 recomendaciones prioritarias listas para revisar."
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasoExplicacionItem(
                    icono = Icons.Outlined.Lightbulb,
                    titulo = "3. Chatbox interactivo",
                    descripcion = "Con el botón 'Consultar al Chatbox IA' puedes conversar libremente con el asistente sobre tus productos estrella, metas y costos."
                )

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
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
                            tint = DarkBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tus datos se procesan de forma anónima y segura únicamente para generar tus recomendaciones.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
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
                .background(BgGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = BrandPurple,
                modifier = Modifier.size(20.dp)
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

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextGray, fontSize = 14.sp)
        Text(value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}