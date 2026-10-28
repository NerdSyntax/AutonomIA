package com.nerdsyntax.juntalucas.feature.dashboard.ui

import androidx.compose.foundation.background
import com.nerdsyntax.juntalucas.feature.dashboard.ui.components.DashboardPeriodSelector
import androidx.compose.foundation.clickable
import com.nerdsyntax.juntalucas.feature.dashboard.ui.components.DashboardPeriodDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import com.nerdsyntax.juntalucas.feature.dashboard.domain.DashboardPeriodFilter
import androidx.compose.runtime.*
import com.nerdsyntax.juntalucas.feature.dashboard.ui.components.DashboardCharts
import com.nerdsyntax.juntalucas.feature.dashboard.ui.components.ExpenseDistributionCard

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val GreenPos = Color(0xFF10B981)
private val RedNeg = Color(0xFFEF4444)
private val OrangeWarn = Color(0xFFF59E0B)
private val PurpleAi = Color(0xFF8B5CF6)


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
    onViewAll: () -> Unit = {}
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DarkBlue)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(
            state = state,
            onFilterSelected = onFilterSelected,
            onPeriodApplied = onPeriodApplied
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

            RecentMovementsSection(
                movimientos = state.movimientosRecientes,
                onViewAll = onViewAll
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
@Composable
private fun HeaderSection(
    state: DashboardUiState,
    onFilterSelected: (DashboardPeriodFilter) -> Unit,
    onPeriodApplied: (DashboardPeriodFilter, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightBg)
            .padding(
                top = 48.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Hola, ${state.userName}",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )

                Text(
                    text = state.nombreNegocio,
                    color = DarkBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = {
                    // Pendiente: conectar con el menú.
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFE7ECF1),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "Menú",
                    tint = DarkBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = {
                    // Pendiente: conectar con las notificaciones.
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFE7ECF1),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = DarkBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
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
                    text = "Chat de mi negocio",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )


                Text(
                    text = "Conversa sobre los datos de tu negocio",
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
