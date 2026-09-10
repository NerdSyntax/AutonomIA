package com.nerdsyntax.juntalucas.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Importante para el clic
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
            GoalSection(state)
            KpiGridSection(state)

            AiBannerSection(onClick = onNavigateToAi)

            RecentMovementsSection(state.movimientosRecientes)
            QuickAccessSection()
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
            Column {
                Text("Hola, ${state.userName} \uD83D\uDC4B", color = Color.LightGray, fontSize = 14.sp)
                Text(state.businessName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                Text("Agosto 2026", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text("Personalizado", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
private fun GoalSection(state: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Meta mensual de ventas", color = Color.Gray, fontSize = 14.sp)
                Text("90%", color = DarkBlue, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 0.9f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = PurpleAi,
                trackColor = Color(0xFFE2E8F0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$1.800.000 alcanzados", color = Color.Gray, fontSize = 12.sp)
                Text("Meta: $2.000.000", color = Color.LightGray, fontSize = 12.sp)
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
            modifier = Modifier.weight(1f), title = "Ventas totales", amount = state.ventasTotales,
            trend = "▲ 12,5% vs jul.", trendColor = GreenPos, icon = Icons.Default.ArrowUpward
        )
        KpiCard(
            modifier = Modifier.weight(1f), title = "Gastos totales", amount = state.gastosTotales,
            trend = "▼ 21,7% vs jul.", trendColor = RedNeg, icon = Icons.Default.ArrowDownward
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KpiCard(
            modifier = Modifier.weight(1f), title = "Ganancia", amount = state.ganancia,
            trend = "▼ 25,8% vs jul.", trendColor = RedNeg, icon = Icons.Default.QueryBuilder, amountColor = OrangeWarn
        )
        KpiCard(
            modifier = Modifier.weight(1f), title = "Margen", amount = state.margen,
            trend = "▼ 4,1pp vs jul.", trendColor = RedNeg, icon = Icons.Default.ShowChart, amountColor = OrangeWarn
        )
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier, title: String, amount: String, trend: String,
    trendColor: Color, icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                Icon(icon, contentDescription = null, tint = trendColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(amount, color = if(title == "Gastos totales") RedNeg else amountColor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(trend, color = trendColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AiBannerSection(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)) // Mantiene el efecto del clic dentro de los bordes redondeados
            .background(
                brush = Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF9333EA)))
            )
            .clickable { onClick() } // Permite que el banner detecte el clic
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

@Composable
private fun QuickAccessSection() {
    Column {
        Text("Accesos rápidos", fontWeight = FontWeight.Bold, color = DarkBlue, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickButton(modifier = Modifier.weight(1f), text = "Registrar venta", icon = "💰", bgColor = Color(0xFFECFDF5), textColor = GreenPos)
            QuickButton(modifier = Modifier.weight(1f), text = "Registrar gasto", icon = "🧾", bgColor = Color(0xFFFEF2F2), textColor = RedNeg)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickButton(modifier = Modifier.weight(1f), text = "Agregar producto", icon = "📦", bgColor = Color(0xFFF0F4FA), textColor = DarkBlue)
            QuickButton(modifier = Modifier.weight(1f), text = "Importar ventas", icon = "📁", bgColor = Color(0xFFF3E8FF), textColor = PurpleAi)
        }
    }
}

@Composable
private fun QuickButton(modifier: Modifier, text: String, icon: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}