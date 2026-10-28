package com.nerdsyntax.juntalucas.feature.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AiLoadingScreen(
    state: AiUiState
) {
    val darkBlue = Color(0xFF0F2A4A)
    val brandPurple = Color(0xFF6D28D9)
    val lightPurple = Color(0xFFEDE9FE)
    val greenSuccess = Color(0xFF16A34A)
    val textGray = Color(0xFF475569)

    val pasos = listOf(
        "Recopilando ventas y gastos de ${state.periodoSeleccionado}...",
        "Analizando métricas y generando recomendaciones con IA...",
        "Preparando resultados..."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(lightPurple),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(84.dp),
                color = brandPurple,
                strokeWidth = 4.dp
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = brandPurple,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Analizando tu negocio",
            color = darkBlue,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Estamos procesando la información en tiempo real",
            color = textGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                pasos.forEachIndexed { index, texto ->
                    val completado = state.pasoCargaActual > index
                    val activo = state.pasoCargaActual == index

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        completado -> Color(0xFFE8F5E9)
                                        activo -> lightPurple
                                        else -> Color(0xFFF1F5F9)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                completado -> Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = greenSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                activo -> CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = brandPurple,
                                    strokeWidth = 2.dp
                                )
                                else -> Text(
                                    text = "${index + 1}",
                                    color = textGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = texto,
                            color = if (completado || activo) darkBlue else textGray.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}