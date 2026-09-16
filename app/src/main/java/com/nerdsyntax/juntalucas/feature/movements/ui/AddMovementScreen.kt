package com.nerdsyntax.juntalucas.feature.movements.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovementScreen(
    onNavigateBack: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)

    var fecha by remember { mutableStateOf("") }
    var producto by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descuento by remember { mutableStateOf("") }
    var medioPago by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }

    val totalFormateado = remember(cantidad, precio, descuento) {
        val cantNum = cantidad.toIntOrNull() ?: 0
        val precioNum = precio.replace(".", "").toIntOrNull() ?: 0
        val descNum = descuento.replace(".", "").toIntOrNull() ?: 0

        val total = (cantNum * precioNum) - descNum

        if (total > 0) {
            val formatoChile = NumberFormat.getInstance(Locale("es", "CL"))
            "$${formatoChile.format(total)}"
        } else {
            "$0"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar venta", color = darkBlue, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = darkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                label = "Fecha *",
                value = fecha,
                onValueChange = { fecha = it },
                placeholder = "Ej: 15-09-2026"
            )

            CustomTextField(
                label = "Producto o servicio *",
                value = producto,
                onValueChange = { producto = it },
                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray) }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(
                    label = "Cantidad *", value = cantidad, onValueChange = { cantidad = it },
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    label = "Precio unitario *", value = precio, onValueChange = { precio = it },
                    modifier = Modifier.weight(1f)
                )
            }

            CustomTextField(label = "Descuento (opcional)", value = descuento, onValueChange = { descuento = it })

            CustomTextField(
                label = "Medio de pago *",
                value = medioPago,
                onValueChange = { medioPago = it },
                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray) }
            )

            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                label = { Text("Nota (opcional)", color = Color.Gray) },
                placeholder = { Text("Ej: cliente habitual, encargo especial...", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = darkBlue
                ),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkBlue, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total calculado", color = Color.White, fontSize = 16.sp)
                    Text(totalFormateado, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Button(
                onClick = { onNavigateBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar venta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        placeholder = placeholder?.let { { Text(it, color = Color.LightGray) } },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedBorderColor = Color(0xFF0F2A4A)
        ),
        singleLine = true,
        trailingIcon = trailingIcon
    )
}