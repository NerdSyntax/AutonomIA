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
import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import com.nerdsyntax.juntalucas.core.format.formatClp
import com.nerdsyntax.juntalucas.feature.movements.domain.PaymentMethod
import com.nerdsyntax.juntalucas.feature.movements.domain.SaleDates
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleScreen(
    state: AddSaleUiState,
    onDateChange: (String) -> Unit,
    onProductSelected: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDiscountChange: (String) -> Unit,
    onPaymentChange: (PaymentMethod) -> Unit,
    onNoteChange: (String) -> Unit,
    onRetryCatalog: () -> Unit,
    onCreateProduct: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)

    val context = LocalContext.current
    val totalFormateado = state.total?.let(::formatClp) ?: "—"
    BackHandler(enabled = state.isSaving) { }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar venta", color = darkBlue, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !state.isSaving) {
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

            SaleSelectionField("Fecha *", SaleDates.display(state.date), !state.isSaving) {
                val parts = state.date.split("-").map(String::toInt)
                DatePickerDialog(context, { _, year, month, day ->
                    onDateChange(String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day))
                }, parts[0], parts[1] - 1, parts[2]).show()
            }

            var productMenu by remember { mutableStateOf(false) }
            Box {
                SaleSelectionField("Producto o servicio *", state.selectedProduct?.name ?: "Seleccionar", !state.isSaving && state.products.isNotEmpty()) { productMenu = true }
                DropdownMenu(expanded = productMenu, onDismissRequest = { productMenu = false }) {
                    state.products.forEach { product ->
                        DropdownMenuItem(text = { Text("${product.name} · ${formatClp(product.price)}") }, onClick = {
                            onProductSelected(product.id); productMenu = false
                        })
                    }
                }
            }
            if (state.isCatalogLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.catalogError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetryCatalog, enabled = !state.isSaving) { Text("Reintentar catálogo") }
            }
            if (!state.isCatalogLoading && state.catalogError == null && state.products.isEmpty()) {
                Text("No tienes productos o servicios activos. Crea uno para registrar la venta.", color = Color.Gray)
            }
            if (state.catalogFromCache && state.products.isNotEmpty()) Text("Catálogo guardado en el dispositivo; se validará al guardar.", color = Color.Gray)
            TextButton(onClick = onCreateProduct, enabled = !state.isSaving) { Text("Crear producto o servicio") }
            state.selectedProduct?.takeIf { it.trackStock }?.let {
                Text("Stock disponible: ${it.stock} ${it.unit}", color = Color.Gray)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MovementTextField(
                    label = "Cantidad *", value = state.quantity, onValueChange = onQuantityChange, enabled = !state.isSaving, isNumber = true,
                    modifier = Modifier.weight(1f)
                )
                MovementTextField(
                    label = "Precio unitario (CLP) *", value = state.unitPrice, onValueChange = onPriceChange, enabled = !state.isSaving, isNumber = true,
                    modifier = Modifier.weight(1f)
                )
            }

            MovementTextField(label = "Descuento en CLP (opcional)", value = state.discount,
                onValueChange = onDiscountChange, enabled = !state.isSaving, isNumber = true)

            var paymentMenu by remember { mutableStateOf(false) }
            Box {
                SaleSelectionField("Medio de pago *", state.paymentMethod?.label ?: "Seleccionar", !state.isSaving) { paymentMenu = true }
                DropdownMenu(expanded = paymentMenu, onDismissRequest = { paymentMenu = false }) {
                    PaymentMethod.entries.forEach { payment ->
                        DropdownMenuItem(text = { Text(payment.label) }, onClick = {
                            onPaymentChange(payment); paymentMenu = false
                        })
                    }
                }
            }
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                enabled = !state.isSaving,
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

            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
            Button(
                onClick = onSave,
                enabled = !state.isSaving && !state.isSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (state.isSaving) "Guardando..." else "Guardar venta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SaleSelectionField(label: String, value: String, enabled: Boolean, onClick: () -> Unit) {
    Column {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F2A4A))) {
            Text(value, modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}