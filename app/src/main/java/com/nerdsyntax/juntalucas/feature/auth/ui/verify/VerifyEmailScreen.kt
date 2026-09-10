package com.nerdsyntax.juntalucas.feature.auth.ui.verify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    state: VerifyEmailUiState,
    onCheckVerification: () -> Unit,
    onResendVerification: () -> Unit,
    onLogout: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verifica tu correo", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onLogout) { // Usamos onLogout como acción de volver atrás
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBlue)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFE8F0FE), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = "Correo",
                    tint = darkBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¡Revisa tu correo!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = darkBlue
            )

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = buildAnnotatedString {
                    append("Enviamos un enlace de verificación a\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = darkBlue)) {
                        append(state.currentUser?.email.orEmpty())
                    }
                    append(". Haz clic en el enlace\npara activar tu cuenta.")
                },
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))


            Surface(
                color = Color(0xFFFFF9E6),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿No lo ves? Revisa tu carpeta de spam o correo no deseado.",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF856404)
                )
            }


            state.errorMessage?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            state.successMessage?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = darkBlue, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = onCheckVerification,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
            ) {
                Text(
                    text = if (state.isLoading) "Comprobando..." else "Ya verifiqué mi correo",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            TextButton(
                onClick = onResendVerification,
                enabled = !state.isLoading
            ) {
                Text("Reenviar correo de verificación", color = darkBlue, fontSize = 16.sp)
            }


            TextButton(
                onClick = onLogout,
                enabled = !state.isLoading
            ) {
                Text("Volver al inicio de sesión", color = darkBlue, fontSize = 16.sp)
            }
        }
    }
}