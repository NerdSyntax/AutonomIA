package com.nerdsyntax.juntalucas.feature.auth.ui.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)
    val highlightPurple = Color(0xFF8B5CF6)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(darkBlue)
                .padding(24.dp)
        ) {
            Column {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)) {
                            append("Autonom")
                        }
                        withStyle(style = SpanStyle(color = highlightPurple, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)) {
                            append("IA")
                        }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = "Ventas del mes",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val heights = listOf(0.4f, 0.5f, 0.7f, 0.6f, 0.9f, 1f)
                        heights.forEach { fraction ->
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .fillMaxHeight(fraction)
                                    .background(highlightPurple, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text(
                    text = "Comprende y mejora tu negocio",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkBlue,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Registra tus movimientos, analiza tus resultados y recibe recomendaciones inteligentes para crecer.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))


                @Composable
                fun Tag(text: String) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag("Registro simple")
                    Tag("Análisis automático")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag("IA local")
                    Tag("Sin complicaciones")
                }

                Spacer(modifier = Modifier.weight(1f))


                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
                ) {
                    Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))


                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, darkBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = darkBlue)
                ) {
                    Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))


                Text(
                    text = buildAnnotatedString {
                        append("Al continuar aceptas los ")
                        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, color = darkBlue)) {
                            append("Términos")
                        }
                        append(" y la ")
                        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, color = darkBlue)) {
                            append("Privacidad")
                        }
                    },
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}