package com.nerdsyntax.juntalucas.feature.auth.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(state.email, onEmailChange, Modifier.fillMaxWidth(), label = { Text("Correo electrónico") },
            singleLine = true, enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(state.password, onPasswordChange, Modifier.fillMaxWidth(), label = { Text("Contraseña") },
            supportingText = { Text("Mínimo 8 caracteres") }, singleLine = true, enabled = !state.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(state.confirmPassword, onConfirmPasswordChange, Modifier.fillMaxWidth(),
            label = { Text("Confirmar contraseña") }, singleLine = true, enabled = !state.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        state.errorMessage?.let {
            Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(onRegister, Modifier.fillMaxWidth(), enabled = !state.isLoading) {
            Text(if (state.isLoading) "Creando cuenta..." else "Crear cuenta")
        }
        TextButton(onBack, enabled = !state.isLoading) { Text("Ya tengo una cuenta") }
    }
}
