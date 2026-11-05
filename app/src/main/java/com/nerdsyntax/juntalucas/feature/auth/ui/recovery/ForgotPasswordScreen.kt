package com.nerdsyntax.juntalucas.feature.auth.ui.recovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RecoveryNavy = Color(0xFF0F2A4A)
private val RecoveryPurple = Color(0xFF9333EA)
private val RecoveryIndigo = Color(0xFF6366F1)
private val RecoveryTextGray = Color(0xFF64748B)
private val RecoveryBorder = Color(0xFFDCE3EF)

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendReset: () -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val headerHeight = 190.dp

    val sendReset = {
        if (!state.isLoading) {
            focusManager.clearFocus()
            onSendReset()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(RecoveryNavy)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val panelMinHeight = (maxHeight - headerHeight)
            .coerceAtLeast(0.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Cabecera con los mismos colores del login.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 8.dp,
                        bottom = 24.dp
                    )
            ) {
                IconButton(
                    onClick = onBack,
                    enabled = !state.isLoading,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(color = Color.White)
                        ) {
                            append("Autonom")
                        }

                        withStyle(
                            SpanStyle(color = RecoveryPurple)
                        ) {
                            append("IA")
                        }
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Recupera el acceso a tu cuenta",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = panelMinHeight),
                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp
                ),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 28.dp,
                            vertical = 32.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color(0xFFF3E8FF)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Outlined.LockReset,
                                contentDescription = null,
                                tint = RecoveryPurple,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = RecoveryNavy,
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Ingresa el correo asociado a tu cuenta " +
                                "y te enviaremos un enlace para restablecerla.",
                        color = RecoveryTextGray,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Correo electrónico",
                            color = RecoveryNavy,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading,
                            singleLine = true,
                            isError = state.errorMessage != null,
                            placeholder = {
                                Text(
                                    text = "ejemplo@correo.com",
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.Email,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    sendReset()
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = RecoveryNavy,
                                unfocusedTextColor = RecoveryNavy,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor =
                                    Color(0xFFF8FAFC),
                                focusedBorderColor = RecoveryPurple,
                                unfocusedBorderColor = RecoveryBorder,
                                cursorColor = RecoveryPurple,
                                focusedLeadingIconColor =
                                    RecoveryPurple,
                                unfocusedLeadingIconColor =
                                    RecoveryTextGray
                            )
                        )
                    }

                    state.errorMessage?.let { message ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF2F2)
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(14.dp),
                                color = Color(0xFFB91C1C),
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    state.successMessage?.let { message ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF5F3FF)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Solicitud enviada",
                                    color = Color(0xFF6D28D9),
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = message,
                                    color = RecoveryNavy,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )

                                Text(
                                    text = "Si no lo encuentras, revisa " +
                                            "la carpeta de spam.",
                                    color = RecoveryTextGray,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        onClick = sendReset,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = if (state.isLoading) {
                                            listOf(
                                                RecoveryIndigo.copy(
                                                    alpha = 0.65f
                                                ),
                                                RecoveryPurple.copy(
                                                    alpha = 0.65f
                                                )
                                            )
                                        } else {
                                            listOf(
                                                RecoveryIndigo,
                                                RecoveryPurple
                                            )
                                        }
                                    )
                                )
                                .heightIn(min = 54.dp)
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ),
                            horizontalArrangement =
                                Arrangement.Center,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )

                                Spacer(
                                    modifier = Modifier.width(10.dp)
                                )
                            }

                            Text(
                                text = when {
                                    state.isLoading -> "Enviando..."
                                    state.successMessage != null ->
                                        "Reenviar enlace"
                                    else -> "Enviar enlace"
                                },
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    TextButton(
                        onClick = onBack,
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = RecoveryNavy
                        )
                    ) {
                        Text(
                            text = "Volver al inicio de sesión",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}