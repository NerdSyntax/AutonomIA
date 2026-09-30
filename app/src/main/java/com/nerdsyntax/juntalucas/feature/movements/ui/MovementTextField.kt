package com.nerdsyntax.juntalucas.feature.movements.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
internal fun MovementTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    isNumber: Boolean = false
) {
    OutlinedTextField(
        value = value,
        enabled = enabled,
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
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
