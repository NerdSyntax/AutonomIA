package com.nerdsyntax.juntalucas.feature.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AiLoadingScreen(
    state: AiUiState,
    vm: AiViewModel,
    onNavigateToChat: () -> Unit,
    onCancel: () -> Unit
) {
    LaunchedEffect(Unit) {
        vm.iniciarAnalisis(onTerminoAnalisis = {
            onNavigateToChat()
        })
    }

    val DarkBlue = Color(0xFF0F2A4A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF6D28D9))

        Spacer(modifier = Modifier.height(32.dp))

        val textoActual = when(state.pasoCargaActual) {
            0 -> "Revisando tus ventas..."
            1 -> "Comparando tus gastos..."
            else -> "Preparando recomendaciones..."
        }

        Text(textoActual, color = Color.White)

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(onClick = {
            vm.onCancelarAnalisis()
            onCancel()
        }) {
            Text("Cancelar análisis", color = Color.Gray)
        }
    }
}