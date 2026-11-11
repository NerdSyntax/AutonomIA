package com.nerdsyntax.juntalucas.feature.onboarding.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val BorderLight = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartingPointScreen(
    state: OnboardingUiState,
    onPuntoPartidaChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onContinueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DarkBlue,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(top = 48.dp, bottom = 22.dp, start = 8.dp, end = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack, enabled = !state.isLoading) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Column {
                    Text(
                        text = "PASO 3 DE 4",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¿Cómo deseas comenzar?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectableCard(
                title = "Registrar manualmente",
                description = "Agrega ventas y gastos uno por uno desde la app.",
                isSelected = state.puntoPartida == "manual",
                onClick = { if (!state.isLoading) onPuntoPartidaChange("manual") }
            )
            SelectableCard(
                title = "Importar desde archivo",
                description = "Sube un archivo CSV o Excel con tus ventas anteriores.",
                isSelected = state.puntoPartida == "importar",
                onClick = { if (!state.isLoading) onPuntoPartidaChange("importar") }
            )
            SelectableCard(
                title = "Comenzar con datos de ejemplo",
                description = "Explora la app con información ficticia para entender cómo funciona.",
                isSelected = state.puntoPartida == "ejemplo",
                onClick = { if (!state.isLoading) onPuntoPartidaChange("ejemplo") }
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(0.5.dp, BorderLight),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resumen de configuración", fontWeight = FontWeight.Bold, color = DarkBlue, fontSize = 14.sp)
                    HorizontalDivider(color = BorderLight)
                    ResumenRow("Negocio", state.nombreNegocio.ifEmpty { "Sin nombre" })
                    ResumenRow("Rubro", state.rubro.ifEmpty { "Sin rubro" })
                    ResumenRow("Actividad", state.tipoActividad.replaceFirstChar { it.uppercase() })
                    ResumenRow("Meta", state.metaMensual.ifEmpty { "$0" })
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Surface(
                onClick = onContinueClick,
                shape = RoundedCornerShape(18.dp),
                color = DarkBlue,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Elegir mi plan",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumenRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 13.sp)
        Text(text = value, color = Color(0xFF0F2A4A), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}