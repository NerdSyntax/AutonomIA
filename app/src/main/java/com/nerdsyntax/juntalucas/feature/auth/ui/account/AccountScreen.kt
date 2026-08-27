package com.nerdsyntax.juntalucas.feature.auth.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountScreen(
    state: AccountUiState,
    onResetPassword: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Mi cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp)); Text("Correo electrónico")
        Text(state.currentUser?.email.orEmpty(), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onResetPassword, Modifier.fillMaxWidth(), enabled = !state.isLoading) {
            Text("Cambiar contraseña")
        }
        Spacer(Modifier.height(8.dp))
        Button(onLogout, Modifier.fillMaxWidth(), enabled = !state.isLoading) { Text("Cerrar sesión") }
        state.errorMessage?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        state.successMessage?.let { Spacer(Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(12.dp))
        TextButton({ showDeleteDialog = true }, enabled = !state.isLoading) {
            Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error)
        }
        TextButton(onBack) { Text("Volver") }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar cuenta") },
            text = { Text("Esta acción eliminará tu cuenta de JuntaLucas. ¿Deseas continuar?") },
            confirmButton = {
                TextButton({ showDeleteDialog = false; onDeleteAccount() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton({ showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}
