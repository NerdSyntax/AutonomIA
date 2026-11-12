package com.nerdsyntax.juntalucas.feature.dashboard.ui

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nerdsyntax.juntalucas.feature.dashboard.domain.DashboardPeriodFilter
import com.nerdsyntax.juntalucas.feature.dashboard.ui.components.BusinessPieChartCard
import com.nerdsyntax.juntalucas.feature.dashboard.ui.components.DashboardCharts
import com.nerdsyntax.juntalucas.feature.dashboard.ui.components.DashboardPeriodSelector
import com.nerdsyntax.juntalucas.feature.dashboard.ui.components.ExpenseDistributionCard
import com.nerdsyntax.juntalucas.feature.paywall.ui.LimitReachedDialog
import com.nerdsyntax.juntalucas.feature.paywall.ui.PaywallScreen
import java.text.NumberFormat
import java.util.Locale

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val GreenPos = Color(0xFF10B981)
private val RedNeg = Color(0xFFEF4444)
private val OrangeWarn = Color(0xFFF59E0B)
private val PurpleAi = Color(0xFF8B5CF6)
private val GradientPurpleStart = Color(0xFF6366F1)
private val GradientPurpleEnd = Color(0xFF9333EA)
private val SilverStart = Color(0xFFE2E8F0)
private val SilverEnd = Color(0xFF94A3B8)

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNavigateToAi: () -> Unit,
    onNavigateToMonthlyAnalysis: () -> Unit,
    onPeriodApplied: (DashboardPeriodFilter, String, String) -> Unit,
    onFilterSelected: (DashboardPeriodFilter) -> Unit = {},
    onCustomStart: (String) -> Unit = {},
    onCustomEnd: (String) -> Unit = {},
    onRetry: () -> Unit = {},
    onViewAll: () -> Unit = {},
    onQuickAddSale: () -> Unit = {},
    onExportCsv: (Context) -> Unit = {},
    notifications: @Composable () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    var mostrarPantallaPlanes by rememberSaveable { mutableStateOf(false) }

    val openPlans: () -> Unit = {
        mostrarPantallaPlanes = true
    }

    if (state.mostrarPaywallSuscripcion && !mostrarPantallaPlanes) {
        LimitReachedDialog(
            fechaReinicio = "próximo ciclo mensual",
            customMessage = state.mensajePaywallPersonalizado,
            onDismiss = { viewModel.ocultarPaywall() },
            onUpgrade = {
                viewModel.ocultarPaywall()
                openPlans()
            }
        )
    }

    if (mostrarPantallaPlanes) {
        Dialog(
            onDismissRequest = { mostrarPantallaPlanes = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            PaywallScreen(
                onClose = { mostrarPantallaPlanes = false },
                onSelectPlan = {},
                currentPlanId = state.planActual
            )
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DarkBlue)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBg)
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(
                state = state,
                onFilterSelected = onFilterSelected,
                onPeriodApplied = onPeriodApplied,
                notifications = notifications,
                onBadgeClick = openPlans
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.errorMessage?.let { message ->
                    Column {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )

                        TextButton(onClick = onRetry) {
                            Text("Reintentar")
                        }
                    }
                }

                GoalSection(state)

                KpiGridSection(state)

                AiAccessSection(
                    onChatClick = onNavigateToAi,
                    onMonthlyClick = onNavigateToMonthlyAnalysis
                )

                DashboardCharts(
                    state = state,
                    onRetry = onRetry
                )
                ExpenseDistributionCard(
                    categorias = state.distribucionGastos,
                    productos = state.ventasPorProducto,
                    isLoading = state.graficosCargando,
                    errorMessage = state.errorGraficos
                )
                BusinessPieChartCard(
                    categorias = state.distribucionGastos,
                    productos = state.ventasPorProducto,
                    isLoading = state.graficosCargando,
                    errorMessage = state.errorGraficos
                )

                RecentMovementsSection(
                    movimientos = state.movimientosRecientes,
                    onViewAll = onViewAll
                )

                Button(
                    onClick = { onExportCsv(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPos)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Exportar CSV",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(GradientPurpleStart, GradientPurpleEnd)))
                .clickable { onQuickAddSale() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Registrar nueva venta",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun DashboardPurchaseDialog(
    purchase: DashboardPurchaseState,
    onDismiss: () -> Unit,
    onSelectPlan: (String) -> Unit,
    onRetry: () -> Unit,
    onBackToPlans: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = !purchase.isSaving,
            dismissOnClickOutside = !purchase.isSaving
        )
    ) {
        if (!purchase.isSaving && !purchase.isSuccess && purchase.errorMessage == null) {
            // Se conserva la firma actual de PaywallScreen. Los cambios de su
            // formulario se harán en el siguiente paso, dentro de ese archivo.
            PaywallScreen(
                onClose = onDismiss,
                onSelectPlan = onSelectPlan
            )
        } else {
            DashboardPurchaseStatus(
                purchase = purchase,
                onDismiss = onDismiss,
                onRetry = onRetry,
                onBackToPlans = onBackToPlans
            )
        }
    }
}

@Composable
private fun DashboardPurchaseStatus(
    purchase: DashboardPurchaseState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onBackToPlans: () -> Unit
) {
    val planName = when (purchase.planId) {
        "FREE" -> "AutonomIA Gratis"
        "PLUS" -> "AutonomIA Plus"
        "PRO" -> "AutonomIA Pro"
        else -> "Tu plan"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (purchase.isSaving) {
                        CircularProgressIndicator(color = PurpleAi)
                    } else {
                        Icon(
                            imageVector = if (purchase.isSuccess) Icons.Default.Check else Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = if (purchase.isSuccess) GreenPos else RedNeg,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Text(
                        text = when {
                            purchase.isSaving -> "Guardando el plan…"
                            purchase.isSuccess -> "Plan guardado"
                            else -> "No se pudo guardar el plan"
                        },
                        color = DarkBlue,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(planName, color = PurpleAi, fontWeight = FontWeight.Bold)
                    Text(
                        text = when {
                            purchase.isSaving -> "Estamos esperando la confirmación del guardado."
                            purchase.isSuccess -> "Tu suscripción de demostración se actualizó correctamente."
                            else -> purchase.errorMessage ?: "Reintenta el guardado para continuar."
                        },
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Modo demostración · Sin cobros reales",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (purchase.isSuccess) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                ) {
                    Text("Volver a Inicio")
                }
            } else if (!purchase.isSaving) {
                if (purchase.planId != null) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                    ) {
                        Text("Reintentar guardado")
                    }
                }
                TextButton(onClick = onBackToPlans, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver a los planes", color = DarkBlue)
                }
            }
        }
    }
}

@Composable
private fun PlanBadge(plan: String, onClick: () -> Unit) {
    val (texto, brush, textColor) = when (plan) {
        "PRO" -> Triple(
            "Pro",
            Brush.horizontalGradient(listOf(SilverStart, SilverEnd)),
            DarkBlue
        )
        "PLUS" -> Triple(
            "Plus",
            Brush.horizontalGradient(listOf(GradientPurpleStart, GradientPurpleEnd)),
            Color.White
        )
        else -> Triple(
            "Gratis",
            Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.2f))),
            Color.White.copy(alpha = 0.9f)
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(brush)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun HeaderSection(
    state: DashboardUiState,
    onFilterSelected: (DashboardPeriodFilter) -> Unit,
    onPeriodApplied: (DashboardPeriodFilter, String, String) -> Unit,
    notifications: @Composable () -> Unit,
    onBadgeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DarkBlue,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(
                top = 48.dp,
                bottom = 24.dp,
                start = 20.dp,
                end = 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hola, ${state.userName}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PlanBadge(plan = state.planActual, onClick = onBadgeClick)
                }
                Text(
                    text = state.nombreNegocio,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            notifications()
        }

        DashboardPeriodSelector(
            selectedFilter = state.selectedFilter,
            dateLabel = state.periodLabel,
            initialStart = state.customStart,
            initialEnd = state.customEnd,
            onFilterSelected = onFilterSelected,
            onPeriodApplied = onPeriodApplied
        )
    }
}

@Composable
private fun GoalSection(state: DashboardUiState) {
    val progress = if (state.metaMensual > 0) {
        (state.ventasMesActual.toDouble() / state.metaMensual).coerceIn(0.0, 1.0).toFloat()
    } else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Meta mensual de ventas", color = Color.Gray, fontSize = 14.sp)
                Text(if (state.metaMensual > 0) "${(progress * 100).toInt()}%" else "Sin meta", color = DarkBlue, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = PurpleAi,
                trackColor = Color(0xFFE2E8F0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${formatPesos(state.ventasMesActual)} alcanzados", color = Color.Gray, fontSize = 12.sp)
                Text(if (state.metaMensual > 0) "Meta: ${formatPesos(state.metaMensual)}" else "Configúrala en Negocio", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun KpiGridSection(state: DashboardUiState) {
    val verde = Color(0xFF15803D)
    val rojo = Color(0xFFB91C1C)
    val azul = Color(0xFF1D4ED8)
    val morado = Color(0xFF6D28D9)
    val ambar = Color(0xFF92400E)

    var ayuda by remember {
        mutableStateOf<Pair<String, String>?>(null)
    }

    val gananciaBruta = state.gananciaBruta

    val textoGanancia = when {
        state.isLoading -> "Cargando…"
        state.errorMessage != null -> "No disponible"
        gananciaBruta != null -> {
            "$" + NumberFormat
                .getIntegerInstance(Locale.forLanguageTag("es-CL"))
                .format(gananciaBruta)
        }
        state.margen == "Sin ventas" -> "Sin ventas"
        else -> "Faltan costos"
    }

    val colorGanancia = when {
        gananciaBruta == null -> ambar
        gananciaBruta < 0.0 -> rojo
        else -> azul
    }

    val colorMargen = when {
        !state.margen.trim().endsWith("%") -> ambar
        state.margen.trim().startsWith("-") -> rojo
        else -> morado
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(
                modifier = Modifier.weight(1f),
                title = "Ventas totales",
                amount = formatPesos(state.ventasTotales),
                color = verde,
                backgroundColor = Color(0xFFF0FDF4),
                icon = Icons.Default.ArrowUpward
            )

            KpiCard(
                modifier = Modifier.weight(1f),
                title = "Gastos totales",
                amount = formatPesos(state.gastosTotales),
                color = rojo,
                backgroundColor = Color(0xFFFEF2F2),
                icon = Icons.Default.ArrowDownward
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(
                modifier = Modifier.weight(1f),
                title = "Ganancia bruta",
                amount = textoGanancia,
                color = colorGanancia,
                backgroundColor = Color(0xFFEFF6FF),
                icon = Icons.Default.Info,
                onInfoClick = {
                    ayuda = "Ganancia bruta" to
                            "Ventas menos el costo histórico de los productos " +
                            "o servicios vendidos. No descuenta otros gastos, " +
                            "como arriendo o administración. Si una venta no " +
                            "tiene su costo registrado, aparece «Faltan costos»."
                }
            )

            KpiCard(
                modifier = Modifier.weight(1f),
                title = "Margen bruto",
                amount = state.margen,
                color = colorMargen,
                backgroundColor = Color(0xFFF5F3FF),
                icon = Icons.Default.Info,
                onInfoClick = {
                    ayuda = "Margen bruto" to
                            "Es la ganancia bruta dividida entre las ventas, " +
                            "multiplicada por 100. Un margen del 40 % significa " +
                            "que quedan $40 por cada $100 vendidos, antes de " +
                            "descontar otros gastos."
                }
            )
        }
    }

    ayuda?.let { (titulo, mensaje) ->
        AlertDialog(
            onDismissRequest = { ayuda = null },
            title = { Text(titulo) },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = { ayuda = null }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier,
    title: String,
    amount: String,
    color: Color,
    backgroundColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onInfoClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 112.dp)
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    top = 6.dp,
                    bottom = 16.dp
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF475569),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                if (onInfoClick != null) {
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Información sobre $title",
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = amount,
                color = color,
                fontSize = if (amount.any { it.isLetter() }) 14.sp else 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AiAccessSection(
    onChatClick: () -> Unit,
    onMonthlyClick: () -> Unit
) {
    Surface(
        onClick = onChatClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF6366F1),
                            Color(0xFF9333EA)
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Analiza tu negocio con nuestro agente",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Analiza y conversa acerca del rendimiento de tu negocio",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun RecentMovementsSection(movimientos: List<MovimientoUi>, onViewAll: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Movimientos recientes", fontWeight = FontWeight.Bold, color = DarkBlue, fontSize = 16.sp)
                Text("Ver todos", color = DarkBlue, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onViewAll))
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (movimientos.isEmpty()) {
                Text("Aún no tienes movimientos registrados.", color = Color.Gray, fontSize = 14.sp)
            }
            movimientos.forEach { mov ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconColor = if (mov.esIngreso) GreenPos else RedNeg
                    val bgColor = if (mov.esIngreso) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)

                    Box(modifier = Modifier.size(40.dp).background(bgColor, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(if (mov.esIngreso) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, tint = iconColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mov.titulo, fontWeight = FontWeight.SemiBold, color = DarkBlue, fontSize = 14.sp)
                        Text(mov.fecha, color = Color.Gray, fontSize = 12.sp)
                    }
                    Text(mov.montoStr, fontWeight = FontWeight.Bold, color = iconColor)
                }
                if (mov != movimientos.last()) HorizontalDivider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

private fun formatPesos(amount: Long): String = "$" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CL")).format(amount)

private fun pickDate(context: android.content.Context, current: String, onSelected: (String) -> Unit) {
    val valid = current.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"))
    val parts = if (valid) current.split("-").map(String::toInt) else null
    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("America/Santiago"))
    DatePickerDialog(context, { _, year, month, day -> onSelected(String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day)) },
        parts?.get(0) ?: calendar.get(java.util.Calendar.YEAR), (parts?.get(1) ?: calendar.get(java.util.Calendar.MONTH) + 1) - 1,
        parts?.get(2) ?: calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
}
