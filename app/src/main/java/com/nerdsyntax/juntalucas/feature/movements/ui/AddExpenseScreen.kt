package com.nerdsyntax.juntalucas.feature.movements.ui

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nerdsyntax.juntalucas.feature.movements.domain.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(state: AddExpenseUiState, onDescriptionChange: (String) -> Unit, onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit, onDateChange: (String) -> Unit, onTypeChange: (ExpenseType) -> Unit,
    onPaymentChange: (PaymentMethod) -> Unit, onNoteChange: (String) -> Unit, onSave: () -> Unit, onNavigateBack: () -> Unit) {
    val darkBlue = Color(0xFF0F2A4A); val context = LocalContext.current
    BackHandler(enabled = state.isSaving) { }
    Scaffold(topBar = { TopAppBar(title = { Text("Registrar gasto", color = darkBlue, fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = onNavigateBack, enabled = !state.isSaving) { Icon(Icons.Default.ArrowBack, "Volver", tint = darkBlue) } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)) }, containerColor = Color.White) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(Modifier.height(8.dp))
            MovementTextField("Descripción *", state.description, onDescriptionChange, placeholder = "Ej: Compra de harina y azúcar", enabled = !state.isSaving)
            MovementTextField("Categoría *", state.category, onCategoryChange, placeholder = "Ej: Insumos", enabled = !state.isSaving)
            MovementTextField("Monto (CLP) *", state.amount, onAmountChange, placeholder = "Ej: 32.400", enabled = !state.isSaving, isNumber = true)
            SelectionField("Fecha *", SaleDates.display(state.date), !state.isSaving) {
                val parts = state.date.split("-").map(String::toInt)
                DatePickerDialog(context, { _, year, month, day -> onDateChange(String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day)) }, parts[0], parts[1] - 1, parts[2]).show()
            }
            var typeMenu by remember { mutableStateOf(false) }
            Box { SelectionField("Tipo de gasto *", state.type.label, !state.isSaving) { typeMenu = true }
                DropdownMenu(typeMenu, { typeMenu = false }) { ExpenseType.entries.forEach { type -> DropdownMenuItem(text = { Text(type.label) }, onClick = { onTypeChange(type); typeMenu = false }) } } }
            var paymentMenu by remember { mutableStateOf(false) }
            Box { SelectionField("Medio de pago *", state.paymentMethod?.label ?: "Seleccionar", !state.isSaving) { paymentMenu = true }
                DropdownMenu(paymentMenu, { paymentMenu = false }) { PaymentMethod.entries.forEach { payment -> DropdownMenuItem(text = { Text(payment.label) }, onClick = { onPaymentChange(payment); paymentMenu = false }) } } }
            OutlinedTextField(value = state.note, onValueChange = onNoteChange, enabled = !state.isSaving, label = { Text("Nota (opcional)") }, placeholder = { Text("Información adicional...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2)
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
            Button(onClick = onSave, enabled = !state.isSaving && !state.isSuccess, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = darkBlue), shape = RoundedCornerShape(12.dp)) { Text(if (state.isSaving) "Guardando..." else "Guardar gasto", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SelectionField(label: String, value: String, enabled: Boolean, onClick: () -> Unit) {
    Column { Text(label, color = Color.Gray, fontSize = 12.sp)
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFE2E8F0)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F2A4A))) {
            Text(value, modifier = Modifier.weight(1f)); Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}
