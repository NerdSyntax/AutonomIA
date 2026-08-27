package com.nerdsyntax.juntalucas.feature.auth.ui.verify

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VerifyEmailScreen(
    state: VerifyEmailUiState,
    onCheckVerification: () -> Unit,
    onResendVerification: () -> Unit,
    onLogout: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Verifica tu correo", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp)); Text("Enviamos un correo de verificación a:")
        Spacer(Modifier.height(4.dp)); Text(state.currentUser?.email.orEmpty(), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp)); Text("Abre el correo, verifica tu cuenta y vuelve a JuntaLucas.")
        state.errorMessage?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        state.successMessage?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(24.dp))
        Button(onCheckVerification, Modifier.fillMaxWidth(), enabled = !state.isLoading) {
            Text(if (state.isLoading) "Comprobando..." else "Ya verifiqué mi correo")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onResendVerification, Modifier.fillMaxWidth(), enabled = !state.isLoading) {
            Text("Reenviar correo")
        }
        TextButton(onLogout, enabled = !state.isLoading) { Text("Cerrar sesión") }
    }
}
