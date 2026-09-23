package com.nerdsyntax.juntalucas.feature.business.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

@Composable
fun AddProductScreen(
    state: AddProductUiState,
    vm: AddProductViewModel,
    onNavigateBack: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)
    val greenPos = Color(0xFF10B981)
    val lightBg = Color(0xFFF8FAFC)

    val symbols = DecimalFormatSymbols().apply { groupingSeparator = '.' }
    val formatter = DecimalFormat("#,###", symbols)

    val gananciaStr = "$${formatter.format(state.ganancia)}"
    val margenStr = "${state.margen.toInt()}%"

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkBlue, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(top = 48.dp, bottom = 20.dp, start = 8.dp, end = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar producto o servicio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        },
        containerColor = lightBg
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

            // Selector entre producto y servicioi
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypeButton("Producto", Icons.Default.Inventory, state.isProduct, { vm.onIsProductChange(true) }, Modifier.weight(1f))
                    TypeButton("Servicio", Icons.Default.Settings, !state.isProduct, { vm.onIsProductChange(false) }, Modifier.weight(1f))
                }
            }

            CustomProductField("Nombre *", state.nombre, "Ej: Cupcakes (docena)", onValueChange = vm::onNombreChange)
            CustomProductField("Categoría", state.categoria, onValueChange = vm::onCategoriaChange, trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray) })
            CustomProductField("Precio de venta *", state.precio, "Ej: 21.000", isNumber = true, onValueChange = vm::onPrecioChange)
            CustomProductField("Costo estimado", state.costo, "Ej: 12.000", isNumber = true, onValueChange = vm::onCostoChange)

            OutlinedTextField(
                value = state.descripcion, onValueChange = vm::onDescripcionChange,
                label = { Text("Descripción (opcional)", color = Color.Gray) },
                placeholder = { Text("Características del producto...", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = darkBlue
                ),
                minLines = 2
            )

            if (state.isProduct) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomProductField("Stock actual", state.stockActual, isNumber = true, onValueChange = vm::onStockActualChange, modifier = Modifier.weight(1f))
                    CustomProductField("Stock mínimo", state.stockMinimo, isNumber = true, onValueChange = vm::onStockMinimoChange, modifier = Modifier.weight(1f))
                    CustomProductField("Unidad", state.unidad, onValueChange = vm::onUnidadChange, modifier = Modifier.weight(1f), trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray) })
                }
            }

            //calculadora
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cálculo automático", color = darkBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Ganancia estimada", color = Color.Gray, fontSize = 12.sp)
                            Text(gananciaStr, color = if (state.isProfitable) greenPos else Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text("Margen estimado", color = Color.Gray, fontSize = 12.sp)
                            Text(margenStr, color = if (state.isProfitable) greenPos else Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(30.dp))
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Estado activo", fontWeight = FontWeight.SemiBold, color = darkBlue, fontSize = 15.sp)
                        Text("Visible y disponible para registrar ventas", color = Color.Gray, fontSize = 13.sp)
                    }
                    Switch(
                        checked = state.isActive,
                        onCheckedChange = vm::onIsActiveChange,
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

            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onNavigateBack, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)), colors = ButtonDefaults.outlinedButtonColors(contentColor = darkBlue)
                ) { Text("Cancelar", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = { vm.saveProduct(onSuccess = onNavigateBack) },
                    modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
                ) { Text("Guardar", fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TypeButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = if (isSelected) Color(0xFF0F2A4A) else Color.White
    val contentColor = if (isSelected) Color.White else Color.Gray
    val border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
    Surface(modifier = modifier.height(44.dp).clickable { onClick() }, color = bgColor, shape = RoundedCornerShape(8.dp), border = border) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = contentColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun CustomProductField(label: String, value: String, placeholder: String? = null, isNumber: Boolean = false, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, trailingIcon: @Composable (() -> Unit)? = null) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label, color = Color.Gray) },
        placeholder = placeholder?.let { { Text(it, color = Color.LightGray) } }, modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedBorderColor = Color(0xFF0F2A4A)
        ),
        singleLine = true, trailingIcon = trailingIcon, keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
    )
}