package com.nerdsyntax.juntalucas.feature.onboarding.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val TextGray = Color(0xFF475569)
private val BorderLight = Color(0xFFE2E8F0)
private val BrandPurple = Color(0xFF7C3AED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessInfoScreen(
    state: OnboardingUiState,
    onNombreChange: (String) -> Unit,
    onRubroChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onComunaChange: (String) -> Unit,
    onMetaChange: (String) -> Unit,
    onContinueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DarkBlue,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(top = 48.dp, bottom = 22.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Text(
                    text = "PASO 1 DE 3",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cuéntanos sobre tu negocio",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(0.5.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Información esencial",
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    OnboardingTextField(
                        label = "Nombre del negocio *",
                        value = state.nombreNegocio,
                        onValueChange = onNombreChange,
                        placeholder = "Ej: Pastelería Dulce Hogar"
                    )

                    OnboardingTextField(
                        label = "Rubro o categoría *",
                        value = state.rubro,
                        onValueChange = onRubroChange,
                        placeholder = "Ej: Repostería, Ferretería, etc."
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(0.5.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Datos opcionales",
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    OnboardingTextField(
                        label = "Región",
                        value = state.region,
                        onValueChange = onRegionChange,
                        placeholder = "Ej: Región Metropolitana"
                    )

                    OnboardingTextField(
                        label = "Comuna",
                        value = state.comuna,
                        onValueChange = onComunaChange,
                        placeholder = "Ej: Providencia"
                    )

                    OnboardingTextField(
                        label = "Meta mensual de ventas (CLP)",
                        value = state.metaMensual,
                        onValueChange = onMetaChange,
                        placeholder = "Ej: 1500000",
                        supportingText = "Solo números, sin puntos ni signos."
                    )
                }
            }
            state.errorMessage?.let { error ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFB91C1C),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = onContinueClick,
                shape = RoundedCornerShape(18.dp),
                color = DarkBlue,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continuar",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnboardingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    supportingText: String? = null
) {
    Column {
        Text(
            text = label,
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextGray.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BorderLight,
                focusedBorderColor = BrandPurple,
                unfocusedContainerColor = LightBg.copy(alpha = 0.5f),
                focusedContainerColor = Color.White
            )
        )
        if (supportingText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = supportingText,
                color = TextGray.copy(alpha = 0.8f),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}