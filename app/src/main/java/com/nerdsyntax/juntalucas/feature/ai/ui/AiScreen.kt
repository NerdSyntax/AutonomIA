package com.nerdsyntax.juntalucas.feature.ai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF0F2A4A)
private val BrandPurple = Color(0xFF6D28D9)
private val LightPurple = Color(0xFFEDE9FE)
private val GreenSuccess = Color(0xFF16A34A)
private val RedDanger = Color(0xFFDC2626)
private val BlueBadge = Color(0xFF3B82F6)
private val BgGray = Color(0xFFF8FAFC)
private val PrivacyBg = Color(0xFFEFF6FF)
private val TextGray = Color(0xFF475569)

@Composable
fun AiScreen(
    state: AiUiState,
    vm: AiViewModel,
    onNavigateToAddSale: () -> Unit
) {
    if (state.mostrarAdvertencia) {
        AiInsufficientDataScreen(
            onNavigateToAddSale = onNavigateToAddSale,
            onNavigateBack = { vm.onOcultarAdvertencia() }
        )
    } else {
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
                        Text("INTELIGENCIA ARTIFICIAL", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Análisis inteligente", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { vm.onAnalizarClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analizar mi negocio", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        modifier = Modifier.clickable { }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { }
                    ) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ver historial de análisis",
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