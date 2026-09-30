package com.nerdsyntax.juntalucas.feature.onboarding.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitySelectionScreen(
    state: OnboardingUiState,
    onTipoActividadChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onContinueClick: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("¿Qué ofreces en tu negocio?", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectableCard(
                title = "Vendo productos",
                description = "Ropa, alimentos, artículos, insumos u objetos físicos.",
                isSelected = state.tipoActividad == "productos",
                onClick = { onTipoActividadChange("productos") }
            )
            SelectableCard(
                title = "Ofrezco servicios",
                description = "Cortes, reparaciones, asesorías, clases u otro servicio.",
                isSelected = state.tipoActividad == "servicios",
                onClick = { onTipoActividadChange("servicios") }
            )
            SelectableCard(
                title = "Productos y servicios",
                description = "Combinas la venta de productos con la entrega de servicios.",
                isSelected = state.tipoActividad == "ambos",
                onClick = { onTipoActividadChange("ambos") }
            )

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
