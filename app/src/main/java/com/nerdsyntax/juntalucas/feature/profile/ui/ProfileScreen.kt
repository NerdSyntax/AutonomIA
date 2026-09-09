package com.nerdsyntax.juntalucas.feature.profile.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreen(state: ProfileUiState, onAccountClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Perfil", style = MaterialTheme.typography.headlineMedium)
            if (state.email.isNotBlank()) Text(text = state.email)
            androidx.compose.material3.Button(onClick = onAccountClick) { Text("Mi cuenta") }
        }
    }
}
