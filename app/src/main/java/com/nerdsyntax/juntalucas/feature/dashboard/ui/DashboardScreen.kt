package com.nerdsyntax.juntalucas.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(state: DashboardUiState, onAccountClick: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("JuntaLucas", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp)); Text("Sesión iniciada correctamente")
        Spacer(Modifier.height(4.dp)); Text(state.email, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))
        Button(onAccountClick) { Text("Mi cuenta") }
    }
}
