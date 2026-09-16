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

@Composable
fun SelectableCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, darkBlue) else BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0F4FA) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = darkBlue)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = darkBlue)
            )
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartingPointScreen(
    state: OnboardingUiState,
    onPuntoPartidaChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onFinishSetup: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("¿Cómo deseas comenzar?", color = Color.White, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelectableCard(
                title = "Registrar manualmente",
                description = "Agrega ventas y gastos uno por uno desde la app.",
                isSelected = state.puntoPartida == "manual",
                onClick = { if (!state.isLoading) onPuntoPartidaChange("manual") }
            )
            SelectableCard(
                title = "Importar desde archivo",
                description = "Sube un archivo CSV o Excel con tus ventas anteriores.",
                isSelected = state.puntoPartida == "importar",
                onClick = { if (!state.isLoading) onPuntoPartidaChange("importar") }
            )
            SelectableCard(
                title = "Comenzar con datos de ejemplo",
                description = "Explora la app con información ficticia para entender cómo funciona.",
                isSelected = state.puntoPartida == "ejemplo",
                onClick = { if (!state.isLoading) onPuntoPartidaChange("ejemplo") }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen de configuración", fontWeight = FontWeight.Bold, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Negocio", color = Color.Gray); Text(state.nombreNegocio.ifEmpty { "Sin nombre" }) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Rubro", color = Color.Gray); Text(state.rubro.ifEmpty { "Sin rubro" }) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Actividad", color = Color.Gray); Text(state.tipoActividad) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Meta mensual", color = Color.Gray); Text(state.metaMensual.ifEmpty { "$0" }) }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = onFinishSetup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
            ) {
                Text(if (state.isLoading) "Guardando..." else "Finalizar configuración", fontSize = 16.sp)
            }
        }
    }
}
