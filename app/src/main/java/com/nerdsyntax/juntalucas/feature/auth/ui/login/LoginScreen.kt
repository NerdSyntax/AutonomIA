package com.nerdsyntax.juntalucas.feature.auth.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)
    val highlightPurple = Color(0xFF8B5CF6)


    var passwordVisible by remember { mutableStateOf(false) }
    var rememberSession by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkBlue)
                .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 32.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp)) {
                        append("Autonom")
                    }
                    withStyle(style = SpanStyle(color = highlightPurple, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp)) {
                        append("IA")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bienvenido/a de vuelta",
                color = Color.LightGray,
                fontSize = 16.sp
            )
        }

    //formulario
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {


            Text("Correo electrónico", color = Color.DarkGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("ejemplo@correo.com", color = Color.LightGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = darkBlue
                )
            )

            Spacer(modifier = Modifier.height(20.dp))


            Text("Contraseña", color = Color.DarkGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Mostrar contraseña", tint = Color.Gray)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = darkBlue
                )
            )


            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberSession,
                        onCheckedChange = { rememberSession = it },
                        colors = CheckboxDefaults.colors(checkedColor = darkBlue)
                    )
                    Text("Recordar sesión", color = Color.DarkGray, fontSize = 14.sp)
                }

                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = darkBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = !state.isLoading) { onForgotPasswordClick() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))


            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
            ) {
                Text(
                    text = if (state.isLoading) "Iniciando..." else "Iniciar sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                Text("o", modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray, fontSize = 14.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(32.dp))


            OutlinedButton(
                onClick = onGoogleClick,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
            ) {

                Text("G", fontWeight = FontWeight.Bold, color = Color.Red, modifier = Modifier.padding(end = 8.dp))
                Text("Continuar con Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.weight(1f))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "¿No tienes una cuenta? ", color = Color.Gray)
                Text(
                    text = "Regístrate",
                    fontWeight = FontWeight.Bold,
                    color = darkBlue,
                    modifier = Modifier.clickable(enabled = !state.isLoading) { onRegisterClick() }
                )
            }
        }
    }
}
