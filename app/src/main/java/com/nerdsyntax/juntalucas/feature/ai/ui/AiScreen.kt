package com.nerdsyntax.juntalucas.feature.ai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF0F2A4A)
private val BrandPurple = Color(0xFF6D28D9)
private val BgGray = Color(0xFFF8FAFC)
private val TextGray = Color(0xFF475569)

@Composable
fun AiScreen(
    state: AiUiState,
    vm: AiViewModel,
    onNavigateToAddSale: () -> Unit,
    onNavigateToLoading: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToMonthly: () -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF101A33))
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 32.dp,
                    bottom = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Autonom",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "IA",
                    color = Color(0xFFA78BFA),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                onClick = onNavigateToChat,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = BrandPurple
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Chat del negocio",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Conversa con tu asistente",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Surface(
                onClick = onNavigateToMonthly,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF0EBFF),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFA78BFA)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Análisis mensual",
                            color = DarkBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Revisa cada mes",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = BrandPurple
                    )
                }
            }

            Surface(
                onClick = onNavigateToHistory,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.07f)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 14.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFC4B5FD),
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Ver historial de análisis",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFC4B5FD),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Periodo a analizar",
                color = DarkBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (state.isLoadingData) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPurple)
                }
            } else {
                if (state.periodos.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.periodos.forEach { periodo ->
                            FilterChip(
                                selected = state.periodoSeleccionado == periodo,
                                onClick = {
                                    vm.onPeriodoSelected(periodo)
                                },
                                label = {
                                    Text(text = periodo)
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    labelColor = TextGray,
                                    selectedContainerColor = Color(0xFFEDE9FE),
                                    selectedLabelColor = BrandPurple
                                )
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No hay periodos disponibles.",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Datos disponibles para el análisis",
                            color = DarkBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        StatRow(
                            label = "Ventas registradas",
                            value = state.ventasRegistradas.toString(),
                            valueColor = Color(0xFF16A34A)
                        )

                        StatRow(
                            label = "Gastos registrados",
                            value = state.gastosRegistrados.toString(),
                            valueColor = Color(0xFFDC2626)
                        )

                        StatRow(
                            label = "Productos en catálogo",
                            value = state.productosCatalogo.toString(),
                            valueColor = DarkBlue
                        )

                        StatRow(
                            label = "Servicios en catálogo",
                            value = state.serviciosCatalogo.toString(),
                            valueColor = DarkBlue
                        )

                        HorizontalDivider(
                            color = Color(0xFFE2E8F0)
                        )

                        Text(
                            text = "Periodo de comparación",
                            color = TextGray,
                            fontSize = 13.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = state.periodoComparacion.ifBlank {
                                    "Sin periodo de comparación"
                                },
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                ),
                                color = Color(0xFF2563EB),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = TextGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}