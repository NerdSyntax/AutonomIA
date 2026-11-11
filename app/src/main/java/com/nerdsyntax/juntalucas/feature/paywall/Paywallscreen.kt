package com.nerdsyntax.juntalucas.feature.paywall.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val GreenPos = Color(0xFF10B981)
private val GradientPurpleStart = Color(0xFF6366F1)
private val GradientPurpleEnd = Color(0xFF9333EA)
private val PurpleBrush = Brush.horizontalGradient(listOf(GradientPurpleStart, GradientPurpleEnd))

data class PlanUiModel(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val period: String,
    val isPopular: Boolean = false,
    val isCurrent: Boolean = false,
    val features: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    currentPlan: String = "FREE",
    onClose: () -> Unit,
    onSelectPlan: (String) -> Unit
) {
    val planes = listOf(
        PlanUiModel(
            id = "FREE",
            name = "AutonomIA Gratis",
            description = "Obtén ayuda esencial para ordenar tus finanzas básicas.",
            price = "CLP 0",
            period = "por mes",
            isCurrent = currentPlan.uppercase() == "FREE",
            features = listOf(
                "Catálogo de hasta 50 productos",
                "100 registros de ventas/gastos al mes",
                "15 consultas mensuales a la IA",
                "1 diagnóstico ejecutivo al cierre de mes",
                "Exportación CSV del mes actual"
            )
        ),
        PlanUiModel(
            id = "PLUS",
            name = "AutonomIA Plus",
            description = "Obtén acceso adicional a potentes funciones de IA para crecer.",
            price = "CLP 4.990",
            period = "por mes",
            isPopular = true,
            isCurrent = currentPlan.uppercase() == "PLUS",
            features = listOf(
                "Todo lo del plan Gratis, además de:",
                "Catálogo de hasta 500 productos",
                "Ventas y gastos ilimitados",
                "50 consultas mensuales a la IA",
                "4 diagnósticos ejecutivos al mes",
                "Exportación CSV de los últimos 6 meses"
            )
        ),
        PlanUiModel(
            id = "PRO",
            name = "AutonomIA Pro",
            description = "El CFO definitivo en tu bolsillo. Sin límites, máximo control.",
            price = "CLP 12.990",
            period = "por mes",
            isCurrent = currentPlan.uppercase() == "PRO",
            features = listOf(
                "Todo lo del plan Plus, además de:",
                "Catálogo y movimientos ilimitados",
                "Consultas a la IA ilimitadas",
                "Diagnósticos y simulaciones a demanda",
                "Historial de análisis interanual",
                "Exportación CSV sin restricciones históricas"
            )
        )
    )

    Scaffold(
        containerColor = LightBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = DarkBlue,
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(top = 48.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Elige tu plan",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Potencia tu negocio con Inteligencia Artificial",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            items(planes) { plan ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PlanCard(plan = plan, onSelect = { onSelectPlan(plan.id) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun Color.opacityCompat(alpha: Float): Color = this.copy(alpha = alpha)

@Composable
private fun PlanCard(plan: PlanUiModel, onSelect: () -> Unit) {
    val isHighlighted = plan.isPopular
    val cardModifier = if (isHighlighted) {
        Modifier
            .fillMaxWidth()
            .border(2.dp, PurpleBrush, RoundedCornerShape(16.dp))
    } else {
        Modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            if (isHighlighted) {
                Text(
                    text = "MÁS POPULAR",
                    color = GradientPurpleStart,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = plan.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = plan.description,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = plan.price,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBlue
                )
                Text(
                    text = " ${plan.period}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (plan.isCurrent) {
                OutlinedButton(
                    onClick = { /* Ya es el plan actual */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Plan actual")
                }
            } else {
                Button(
                    onClick = onSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (isHighlighted) {
                        ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    } else {
                        ButtonDefaults.buttonColors(containerColor = DarkBlue)
                    }
                ) {
                    Box(
                        modifier = if (isHighlighted) {
                            Modifier
                                .fillMaxSize()
                                .background(PurpleBrush, RoundedCornerShape(12.dp))
                        } else {
                            Modifier.fillMaxSize()
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Comenzar", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(16.dp))

            plan.features.forEach { feature ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isHighlighted) GradientPurpleStart else DarkBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = feature,
                        fontSize = 14.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LimitReachedDialog(
    fechaReinicio: String,
    customMessage: String? = null,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFFEF2F2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Límite alcanzado",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = customMessage ?: "Has utilizado toda la cuota de esta función incluida en tu plan Gratis. Tu cuota se reiniciará el $fechaReinicio.",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onUpgrade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PurpleBrush, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Mejorar plan", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = Color.Gray)
                }
            }
        }
    }
}