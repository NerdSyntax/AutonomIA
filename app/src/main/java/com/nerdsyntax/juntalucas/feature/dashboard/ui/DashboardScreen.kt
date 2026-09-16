package com.nerdsyntax.juntalucas.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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


private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val GreenPos = Color(0xFF10B981)
private val RedNeg = Color(0xFFEF4444)
private val OrangeWarn = Color(0xFFF59E0B)
private val PurpleAi = Color(0xFF8B5CF6)

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNavigateToAi: () -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
        HeaderSection(state)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            GoalSection(state)
            KpiGridSection(state)

            AiBannerSection(onClick = onNavigateToAi)

            RecentMovementsSection(state.movimientosRecientes)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection(state: DashboardUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DarkBlue,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Hola, ${state.userEmail}", color = Color.LightGray, fontSize = 14.sp)
                Text(state.nombreNegocio, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = { }, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape).size(40.dp)) {
                    Icon(Icons.Default.GridView, contentDescription = "Menú", tint = Color.White)
                }
                IconButton(onClick = { }, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape).size(40.dp)) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Semana", color = Color.Gray, fontSize = 14.sp)
            Box(modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text("Mes actual", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text("Personalizado", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
private fun GoalSection(state: DashboardUiState) {
    val progress = if (state.metaMensual > 0) {
        (state.ventasTotales.toDouble() / state.metaMensual).coerceIn(0.0, 1.0).toFloat()
    } else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Meta mensual de ventas", color = Color.Gray, fontSize = 14.sp)
                Text("${(progress * 100).toInt()}%", color = DarkBlue, fontWeight = FontWeight.Bold)
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
                Text("${formatPesos(state.ventasTotales)} alcanzados", color = Color.Gray, fontSize = 12.sp)
                Text("Meta: ${if (state.errorMessage == null) formatPesos(state.metaMensual) else "—"}", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun KpiGridSection(state: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KpiCard(
            modifier = Modifier.weight(1f), title = "Ventas totales", amount = formatPesos(state.ventasTotales),
            iconColor = GreenPos, icon = Icons.Default.ArrowUpward
        )
        KpiCard(
            modifier = Modifier.weight(1f), title = "Gastos totales", amount = formatPesos(state.gastosTotales),
            iconColor = RedNeg, icon = Icons.Default.ArrowDownward
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KpiCard(
            modifier = Modifier.weight(1f), title = "Ganancia", amount = formatPesos(state.ganancia),
            iconColor = RedNeg, icon = Icons.Default.QueryBuilder, amountColor = OrangeWarn
        )
        KpiCard(
            modifier = Modifier.weight(1f), title = "Margen", amount = state.margen,
            iconColor = RedNeg, icon = Icons.Default.ShowChart, amountColor = OrangeWarn
        )
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier, title: String, amount: String,
    iconColor: Color, icon: androidx.compose.ui.graphics.vector.ImageVector,
    amountColor: Color = GreenPos
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = Color.Gray, fontSize = 12.sp)
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(amount, color = if(title == "Gastos totales") RedNeg else amountColor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun AiBannerSection(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF9333EA)))
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "IA", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Analizar mi negocio con IA", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Recibe hasta 3 recomendaciones", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Ir", tint = Color.White)
        }
    }
}

@Composable
private fun RecentMovementsSection(movimientos: List<MovimientoUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Movimientos recientes", fontWeight = FontWeight.Bold, color = DarkBlue, fontSize = 16.sp)
                Text("Ver todos", color = DarkBlue, fontSize = 12.sp)
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
