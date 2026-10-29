package com.nerdsyntax.juntalucas.feature.auth.ui.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegister: (String) -> Unit,
    onBack: () -> Unit
) {
    var nombreCompleto by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var documentToShow by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, bottom = 40.dp, start = 24.dp, end = 24.dp)
        ) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Crear cuenta",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Comienza a gestionar tu negocio",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 32.dp, vertical = 36.dp)
            ) {
                Text(
                    text = "Nombre completo",
                    color = DarkBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = nombreCompleto,
                    onValueChange = { nombreCompleto = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Ej: Camila González", color = TextGray.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderLight,
                        focusedBorderColor = BrandPurple,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Correo electrónico",
                    color = DarkBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("tucorreo@ejemplo.com", color = TextGray.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderLight,
                        focusedBorderColor = BrandPurple,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Contraseña",
                    color = DarkBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Mínimo 8 caracteres", color = TextGray.copy(alpha = 0.5f)) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = TextGray)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderLight,
                        focusedBorderColor = BrandPurple,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Confirmar contraseña",
                    color = DarkBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Repite la contraseña", color = TextGray.copy(alpha = 0.5f)) },
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = TextGray)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderLight,
                        focusedBorderColor = BrandPurple,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = BrandPurple)
                    )

                    val legalText = buildAnnotatedString {
                        append("Acepto los ")
                        pushStringAnnotation(tag = "DOC", annotation = "terminos")
                        withStyle(style = SpanStyle(color = DarkBlue, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                            append("Términos")
                        }
                        pop()
                        append(" y la ")
                        pushStringAnnotation(tag = "DOC", annotation = "privacidad")
                        withStyle(style = SpanStyle(color = DarkBlue, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                            append("Privacidad")
                        }
                        pop()
                    }

                    ClickableText(
                        text = legalText,
                        style = TextStyle(fontSize = 13.sp, color = TextGray),
                        onClick = { offset ->
                            legalText.getStringAnnotations(tag = "DOC", start = offset, end = offset)
                                .firstOrNull()?.let { anotacion ->
                                    documentToShow = anotacion.item
                                }
                        }
                    )
                }

                if (!state.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = Color(0xFFDC2626),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Surface(
                    onClick = { onRegister(nombreCompleto) },
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading && acceptedTerms
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (!state.isLoading && acceptedTerms) PrimaryGradient
                                else Brush.horizontalGradient(listOf(Color.LightGray, Color.LightGray))
                            )
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Crear cuenta",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "¿Ya tienes cuenta? ", color = TextGray, fontSize = 14.sp)
                    Text(
                        text = "Inicia sesión",
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable(enabled = !state.isLoading) { onBack() }
                    )
                }
            }
        }
    }

    if (documentToShow != null) {
        ModalBottomSheet(
            onDismissRequest = { documentToShow = null },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .fillMaxHeight(0.8f)
            ) {
                Text(
                    text = if (documentToShow == "terminos") "Términos y Condiciones" else "Política de Privacidad",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(color = BorderLight)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = if (documentToShow == "terminos") {
                            "TÉRMINOS Y CONDICIONES - AutonomIA\n\n1. Naturaleza del Servicio\nAutonomIA es una plataforma tecnológica de registro y análisis de movimientos financieros. Las recomendaciones generadas por la Inteligencia Artificial tienen un fin estrictamente orientativo y no constituyen asesoría financiera, contable o legal profesional.\n\n2. Responsabilidad de la Cuenta\nEl usuario es el único responsable de mantener la confidencialidad de sus credenciales de acceso. Cualquier actividad realizada bajo su cuenta será de su exclusiva responsabilidad.\n\n3. Disponibilidad y Limitación de Responsabilidad\nEl servicio se proporciona 'tal cual' (as is). No garantizamos que la aplicación esté libre de errores o interrupciones. AutonomIA no se hace responsable por pérdidas o daños derivados de decisiones de negocio tomadas en base a los análisis de la plataforma.\n\n4. Uso Correcto\nTe comprometes a ingresar información veraz y a no utilizar la aplicación para fines ilícitos, fraudulentos o que vulneren derechos de terceros."
                        } else {
                            "POLÍTICA DE PRIVACIDAD - AutonomIA\n\n1. Marco Legal Chileno\nEl tratamiento de tus datos personales se realiza en estricto cumplimiento de la legislación chilena, adaptándonos a los estándares de la nueva Ley de Protección de Datos Personales (Ley 21.719).\n\n2. Recopilación y Almacenamiento\nRecopilamos tu nombre, correo electrónico y los datos financieros que ingresas (ventas, gastos, productos). Estos datos se almacenan de forma segura en servidores en la nube (Google Firebase Firestore).\n\n3. Privacidad y Uso de la IA\nLa información ingresada se utiliza exclusivamente para generar métricas y recomendaciones personalizadas para tu negocio. No vendemos, alquilamos ni compartimos tus datos comerciales con terceros para fines publicitarios.\n\n4. Retención y Eliminación de Datos (Derechos ARCO)\nTienes el control total de tu información. Si decides eliminar tu cuenta desde la sección de Perfil, nuestro sistema ejecutará un borrado en cascada inmediato, eliminando permanentemente tu usuario y todos los documentos asociados a tu negocio (ventas, gastos e historial de IA) de nuestras bases de datos."
                        },
                        fontSize = 14.sp,
                        color = TextGray,
                        lineHeight = 22.sp
                    )
                }

                Button(
                    onClick = { documentToShow = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Entendido",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}