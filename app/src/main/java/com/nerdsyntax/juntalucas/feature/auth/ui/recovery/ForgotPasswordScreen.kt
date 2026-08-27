package com.nerdsyntax.juntalucas.feature.auth.ui.recovery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendReset: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Recuperar contraseña", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp)); Text("Ingresa el correo asociado a tu cuenta.")
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(state.email, onEmailChange, Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") }, singleLine = true, enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        state.errorMessage?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        state.successMessage?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(24.dp))
        Button(onSendReset, Modifier.fillMaxWidth(), enabled = !state.isLoading) {
            Text(if (state.isLoading) "Enviando..." else "Enviar correo")
        }
        TextButton(onBack, enabled = !state.isLoading) { Text("Volver") }
    }
}
