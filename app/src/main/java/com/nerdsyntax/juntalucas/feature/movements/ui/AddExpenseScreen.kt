package com.nerdsyntax.juntalucas.feature.movements.ui

import androidx.compose.foundation.BorderStroke
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)

    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Insumos") }
    var monto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("04-09-2026") }
    var tipoGasto by remember { mutableStateOf("Variable") }
    var repetirMensualmente by remember { mutableStateOf(false) }
    var nota by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar gasto", color = darkBlue, fontWeight = FontWeight.Bold) },
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

            CustomExpenseTextField(
                label = "Descripción *",
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = "Ej: Compra de harina y azúcar"
            )

            CustomExpenseTextField(
                label = "Categoría *",
                value = categoria,
                onValueChange = { categoria = it },
                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray) }
            )

            CustomExpenseTextField(
                label = "Monto *",
                value = monto,
                onValueChange = { monto = it },
                placeholder = "Ej: 32.400"
            )

            CustomExpenseTextField(
                label = "Fecha *",
                value = fecha,
                onValueChange = { fecha = it }
            )

            CustomExpenseTextField(
                label = "Tipo de gasto",
                value = tipoGasto,
                onValueChange = { tipoGasto = it },
                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray) }
            )

//switch mensual
            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Repetir mensualmente", fontWeight = FontWeight.SemiBold, color = darkBlue, fontSize = 15.sp)
                        Text("Se generará automáticamente cada mes", color = Color.Gray, fontSize = 13.sp)
                    }
                    Switch(
                        checked = repetirMensualmente,
                        onCheckedChange = { repetirMensualmente = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = darkBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE2E8F0),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                label = { Text("Nota (opcional)", color = Color.Gray) },
                placeholder = { Text("Información adicional...", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = darkBlue
                ),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onNavigateBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar gasto", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CustomExpenseTextField(
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