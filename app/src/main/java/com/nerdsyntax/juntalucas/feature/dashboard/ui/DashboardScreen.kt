package com.nerdsyntax.juntalucas.feature.dashboard.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nerdsyntax.juntalucas.core.format.formatClp
import com.nerdsyntax.juntalucas.feature.dashboard.domain.DashboardPeriodFilter

private val HeaderBlue = Color(0xFF0F2942)
private val BgLight = Color(0xFFF8FAFC)
private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF64748B)
private val GreenIncome = Color(0xFF10B981)
private val GreenLightBg = Color(0xFFD1FAE5)
private val RedExpense = Color(0xFFEF4444)
private val RedLightBg = Color(0xFFFEE2E2)
private val AmberBalance = Color(0xFFF59E0B)
private val PurpleBrand = Color(0xFF7C3AED)
private val PurpleGradientEnd = Color(0xFF9333EA)

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNavigateToAi: () -> Unit = {},
    onFilterSelected: (DashboardPeriodFilter) -> Unit = {},
    onCustomStart: (String) -> Unit = {},
    onCustomEnd: (String) -> Unit = {},
    onRetry: () -> Unit = {},
    onViewAll: () -> Unit = {}
) {
    var nombreFirebase by remember { mutableStateOf("") }

    LaunchedEffect(state.userName) {
        if (state.userName.isBlank() || state.userName.equals("Usuario", ignoreCase = true)) {
            val user = FirebaseAuth.getInstance().currentUser
            val desdeAuth = user?.displayName?.takeIf { it.isNotBlank() }
            if (desdeAuth != null) {
                nombreFirebase = desdeAuth
            } else if (user?.uid != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val desdeDoc = listOf("name", "nombre", "firstName", "fullName", "displayName")
                            .firstNotNullOfOrNull { campo -> doc.getString(campo)?.takeIf { it.isNotBlank() } }
                        nombreFirebase = desdeDoc ?: user.email?.substringBefore("@") ?: "Usuario"
                    }
                    .addOnFailureListener {
                        nombreFirebase = user.email?.substringBefore("@") ?: "Usuario"
                    }
            }
        }
    }

    val nombreBase = if (state.userName.isNotBlank() && !state.userName.equals("Usuario", ignoreCase = true)) {
        state.userName
    } else {
        nombreFirebase
    }

    val primerNombre = nombreBase
        .trim()
        .split(Regex("[\\s._0-9-]+"))
        .firstOrNull { it.length >= 2 }
        ?.lowercase()
        ?.replaceFirstChar { it.uppercase() }
        ?: "Usuario"

    val margenCalculado = remember(state.margen, state.ventasTotales, state.balanceMovimientos) {
        if (state.margen.isNotBlank() && state.margen != "—") {
            state.margen
        } else if (state.ventasTotales > 0L) {
            "${((state.balanceMovimientos * 100) / state.ventasTotales)}%"
        } else {
            "—"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(HeaderBlue)
                .padding(top = 44.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hola, $primerNombre",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.nombreNegocio.ifBlank { "Mi Negocio" },
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = { onRetry() },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.GridView, contentDescription = "Menú", tint = Color.White)
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PeriodTabItem(
                        label = "Semana",
                        selected = state.selectedFilter == DashboardPeriodFilter.WEEK,
                        onClick = { onFilterSelected(DashboardPeriodFilter.WEEK) }
                    )
                    PeriodTabItem(
                        label = "Mes actual",
                        selected = state.selectedFilter == DashboardPeriodFilter.MONTH,
                        onClick = { onFilterSelected(DashboardPeriodFilter.MONTH) }
                    )
                    PeriodTabItem(
                        label = "Personalizado",
                        selected = state.selectedFilter == DashboardPeriodFilter.CUSTOM,
                        onClick = { onFilterSelected(DashboardPeriodFilter.CUSTOM) }
                    )
                }

                if (state.selectedFilter == DashboardPeriodFilter.CUSTOM) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.customStart,
                            onValueChange = onCustomStart,
                            label = { Text("Desde (YYYY-MM-DD)", fontSize = 11.sp, color = Color.LightGray) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                            )
                        )
                        OutlinedTextField(
                            value = state.customEnd,
                            onValueChange = onCustomEnd,
                            label = { Text("Hasta (YYYY-MM-DD)", fontSize = 11.sp, color = Color.LightGray) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonthlyGoalCard(
                alcanzado = state.ventasMesActual.takeIf { it > 0 } ?: state.ventasTotales,
                meta = state.metaMensual
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Ventas totales",
                    value = formatClp(state.ventasTotales),
                    valueColor = GreenIncome,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = GreenIncome,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Gastos totales",
                    value = formatClp(state.gastosTotales),
                    valueColor = RedExpense,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = RedExpense,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Balance de movimientos",
                    value = formatClp(state.balanceMovimientos),
                    valueColor = AmberBalance,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = RedExpense,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Margen",
                    value = margenCalculado,
                    valueColor = AmberBalance,
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = RedExpense,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            AiBannerCard(onClick = onNavigateToAi)

            RecentMovementsListCard(
                movimientos = state.movimientosRecientes,
                onSeeAllClick = onViewAll
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PeriodTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color.White.copy(alpha = 0.22f) else Color.Transparent,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.LightGray.copy(alpha = 0.75f),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun MonthlyGoalCard(
    alcanzado: Long,
    meta: Long
) {
    val porcentaje = if (meta > 0L) ((alcanzado.toFloat() / meta.toFloat()) * 100f).toInt().coerceIn(0, 100) else 0
    val progresoAnimado by animateFloatAsState(
        targetValue = if (meta > 0L) (alcanzado.toFloat() / meta.toFloat()).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "goalProgress"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Meta mensual de ventas", color = TextSecondary, fontSize = 14.sp)
                Text("$porcentaje%", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progresoAnimado)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(PurpleBrand)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${formatClp(alcanzado)} alcanzados", color = TextSecondary, fontSize = 12.sp)
                Text(
                    text = if (meta > 0L) "Meta: ${formatClp(meta)}" else "Sin meta definida",
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun MetricSummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    valueColor: Color,
    icon: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                icon()
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AiBannerCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(PurpleBrand, PurpleGradientEnd)
                )
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Analizar mi negocio con IA",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Recibe hasta 3 recomendaciones",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun RecentMovementsListCard(
    movimientos: List<MovimientoUi>,
    onSeeAllClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Movimientos recientes",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ver todos",
                    color = HeaderBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSeeAllClick() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (movimientos.isEmpty()) {
                Text(
                    text = "Aún no hay movimientos registrados en este periodo.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                movimientos.forEachIndexed { index, item ->
                    val (_, titulo, fecha, montoTexto, esIngreso) = item

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (esIngreso) GreenLightBg else RedLightBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (esIngreso) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (esIngreso) GreenIncome else RedExpense,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = titulo,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = fecha,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Text(
                            text = montoTexto,
                            color = if (esIngreso) GreenIncome else RedExpense,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (index < movimientos.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}