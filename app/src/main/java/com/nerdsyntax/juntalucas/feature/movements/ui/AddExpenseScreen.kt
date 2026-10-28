package com.nerdsyntax.juntalucas.feature.movements.ui

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nerdsyntax.juntalucas.feature.movements.domain.*
import java.util.Locale

private val DarkBlue = Color(0xFF0F2A4A)
private val LightBg = Color(0xFFF8FAFC)
private val TextGray = Color(0xFF475569)
private val BorderLight = Color(0xFFE2E8F0)
private val BrandPurple = Color(0xFF7C3AED)

private val ExpensePurpleGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF6366F1), Color(0xFF9333EA))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    state: AddExpenseUiState,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTypeChange: (ExpenseType) -> Unit,
    onPaymentChange: (PaymentMethod) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler(enabled = state.isSaving) { }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = { if (!state.isSaving) onNavigateBack() },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "NUEVO EGRESO",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Registrar gasto",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ExpensePurpleGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
                        text = "Detalle del egreso",
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    MovementTextField(
                        label = "Descripción *",
                        value = state.description,
                        onValueChange = onDescriptionChange,
                        placeholder = "Ej: Compra de harina y azúcar",
                        enabled = !state.isSaving
                    )

                    MovementTextField(
                        label = "Categoría *",
                        value = state.category,
                        onValueChange = onCategoryChange,
                        placeholder = "Ej: Insumos o Materias primas",
                        enabled = !state.isSaving
                    )

                    MovementTextField(
                        label = "Monto (CLP) *",
                        value = state.amount,
                        onValueChange = onAmountChange,
                        placeholder = "Ej: 32400",
                        enabled = !state.isSaving,
                        isNumber = true
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
                        text = "Clasificación y pago",
                        color = DarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    SelectionField(
                        label = "Fecha *",
                        value = SaleDates.display(state.date),
                        enabled = !state.isSaving,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = BrandPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    ) {
                        val parts = state.date.split("-").map(String::toInt)
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                onDateChange(String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day))
                            },
                            parts[0],
                            parts[1] - 1,
                            parts[2]
                        ).show()
                    }

                    var typeMenu by remember { mutableStateOf(false) }
                    Box {
                        SelectionField(
                            label = "Tipo de gasto *",
                            value = state.type.label,
                            enabled = !state.isSaving
                        ) { typeMenu = true }

                        DropdownMenu(
                            expanded = typeMenu,
                            onDismissRequest = { typeMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            ExpenseType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.label, color = DarkBlue) },
                                    onClick = {
                                        onTypeChange(type)
                                        typeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    var paymentMenu by remember { mutableStateOf(false) }
                    Box {
                        SelectionField(
                            label = "Medio de pago *",
                            value = state.paymentMethod?.label ?: "Seleccionar",
                            enabled = !state.isSaving,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Payments,
                                    contentDescription = null,
                                    tint = BrandPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        ) { paymentMenu = true }

                        DropdownMenu(
                            expanded = paymentMenu,
                            onDismissRequest = { paymentMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            PaymentMethod.entries.forEach { payment ->
                                DropdownMenuItem(
                                    text = { Text(payment.label, color = DarkBlue) },
                                    onClick = {
                                        onPaymentChange(payment)
                                        paymentMenu = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.note,
                        onValueChange = onNoteChange,
                        enabled = !state.isSaving,
                        label = { Text("Nota (opcional)", color = TextGray) },
                        placeholder = { Text("Información adicional del proveedor o boleta...", color = TextGray.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BorderLight,
                            focusedBorderColor = BrandPurple,
                            unfocusedContainerColor = LightBg.copy(alpha = 0.5f),
                            focusedContainerColor = Color.White
                        ),
                        minLines = 2
                    )
                }
            }

            state.errorMessage?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        color = Color(0xFFB91C1C),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (state.isSaving) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50)),
                    color = BrandPurple
                )
            }
            val canSave = !state.isSaving && !state.isSuccess
            Surface(
                onClick = { if (canSave) onSave() },
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent,
                shadowElevation = if (canSave) 4.dp else 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (canSave) ExpensePurpleGradient
                            else Brush.horizontalGradient(listOf(BorderLight, BorderLight))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.isSaving) "Guardando..." else "Guardar gasto",
                        color = if (canSave) Color.White else TextGray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SelectionField(
    label: String,
    value: String,
    enabled: Boolean,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
            color = LightBg.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, BorderLight),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = value,
                    color = DarkBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextGray
                )
            }
        }
    }
}