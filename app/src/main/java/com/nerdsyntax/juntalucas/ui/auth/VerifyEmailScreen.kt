package com.nerdsyntax.juntalucas.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VerifyEmailScreen(
    state: AuthUiState,
    onCheckVerification: () -> Unit,
    onResendVerification: () -> Unit,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Verifica tu correo",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Enviamos un correo de verificación a:"
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = state.currentUser
                ?.email
                .orEmpty(),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Abre el correo, verifica tu cuenta y vuelve a JuntaLucas."
        )

        state.errorMessage?.let {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        state.infoMessage?.let {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onCheckVerification,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text("Ya verifiqué mi correo")
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = onResendVerification,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text("Reenviar correo")
        }

        TextButton(
            onClick = onLogout,
            enabled = !state.isLoading
        ) {
            Text("Cerrar sesión")
        }
    }
}