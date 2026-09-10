package com.nerdsyntax.juntalucas.feature.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val darkBlue = Color(0xFF0F2A4A)

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkBlue)
                    .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Text("Configuración inicial", color = Color.LightGray, fontSize = 14.sp)
                Text("Cuéntanos sobre tu negocio", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color.White, shape = CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).background(Color.Gray, shape = CircleShape))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.nombreNegocio,
                onValueChange = onNombreChange,
                label = { Text("Nombre del negocio *") },
                placeholder = { Text("Pastelería Dulce Hogar") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.rubro,
                onValueChange = onRubroChange,
                label = { Text("Rubro o categoría *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.region,
                onValueChange = onRegionChange,
                label = { Text("Región (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.comuna,
                onValueChange = onComunaChange,
                label = { Text("Comuna (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.metaMensual,
                onValueChange = onMetaChange,
                label = { Text("Meta mensual de ventas (opcional)") },
                supportingText = { Text("Moneda: Pesos chilenos (CLP)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
            ) {
                Text("Continuar", fontSize = 16.sp)
            }
        }
    }
}