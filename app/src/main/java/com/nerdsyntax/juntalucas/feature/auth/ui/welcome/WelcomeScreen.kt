package com.nerdsyntax.juntalucas.feature.auth.ui.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF0F2A4A)
private val BrandPurple = Color(0xFF7C3AED)
private val TextGray = Color(0xFF475569)
private val BorderLight = Color(0xFFE2E8F0)

private val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF6366F1), Color(0xFF9333EA))
)
private val ChartBarGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF9333EA), Color(0xFF6366F1).copy(alpha = 0.6f))
)

@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkBlue)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, letterSpacing = 0.5.sp)) {
                            append("Autonom")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF9333EA), fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)) {
                            append("IA")
                        }
                    }
                )
                Text(
                    text = "Inteligencia financiera para tu negocio",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(18.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = "Proyección de ingresos",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val heights = listOf(0.35f, 0.45f, 0.65f, 0.55f, 0.85f, 1f)
                            heights.forEachIndexed { index, fraction ->
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .fillMaxHeight(fraction)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(ChartBarGradient)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.25f),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 36.dp)
            ) {
                Text(
                    text = "Comprende y mejora tu negocio",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBlue,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Registra tus movimientos, analiza tus resultados y recibe recomendaciones inteligentes para crecer de forma segura.",
                    fontSize = 15.sp,
                    color = TextGray,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FeatureTag(text = "Registro simple", icon = Icons.Outlined.Edit)
                        FeatureTag(text = "Análisis automático", icon = Icons.Outlined.Analytics)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FeatureTag(text = "IA local", icon = Icons.Default.AutoAwesome, isHighlight = true)
                        FeatureTag(text = "Sin complicaciones", icon = Icons.Outlined.Bolt)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(28.dp))
                Surface(
                    onClick = onNavigateToRegister,
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(PrimaryGradient)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Crear cuenta",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, BorderLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue)
                ) {
                    Text(
                        text = "Iniciar sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Al continuar aceptas nuestros ")
                        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, color = DarkBlue, fontWeight = FontWeight.Medium)) {
                            append("Términos")
                        }
                        append(" y la ")
                        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, color = DarkBlue, fontWeight = FontWeight.Medium)) {
                            append("Privacidad")
                        }
                    },
                    fontSize = 12.sp,
                    color = TextGray.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FeatureTag(
    text: String,
    icon: ImageVector,
    isHighlight: Boolean = false
) {
    val bgColor = if (isHighlight) Color(0xFFF3E8FF) else Color(0xFFF1F5F9)
    val contentColor = if (isHighlight) BrandPurple else TextGray

    Surface(
        shape = CircleShape,
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}