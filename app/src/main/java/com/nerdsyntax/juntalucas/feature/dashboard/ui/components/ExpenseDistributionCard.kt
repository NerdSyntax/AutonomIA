package com.nerdsyntax.juntalucas.feature.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nerdsyntax.juntalucas.core.format.formatClp
import com.nerdsyntax.juntalucas.feature.dashboard.ui.GastoCategoriaUi
import com.nerdsyntax.juntalucas.feature.dashboard.ui.VentaProductoUi
import java.text.NumberFormat
import java.util.Locale

private val DistributionTitleColor = Color(0xFF193B63)
private val DefaultExpenseColor = Color(0xFFDC2626)
private val DefaultProductColor = Color(0xFF059669)

private data class DistributionItem(
    val key: String,
    val name: String,
    val amount: Long
)

private data class DistributionColorOption(
    val name: String,
    val color: Color
)

private val DistributionColorOptions = listOf(
    DistributionColorOption("Rojo", Color(0xFFDC2626)),
    DistributionColorOption("Naranjo", Color(0xFFF97316)),
    DistributionColorOption("Amarillo", Color(0xFFCA8A04)),
    DistributionColorOption("Verde", Color(0xFF059669)),
    DistributionColorOption("Celeste", Color(0xFF0891B2)),
    DistributionColorOption("Azul", Color(0xFF2563EB)),
    DistributionColorOption("Violeta", Color(0xFF7C3AED)),
    DistributionColorOption("Rosado", Color(0xFFDB2777)),
    DistributionColorOption("Gris", Color(0xFF64748B))
)

@Composable
fun ExpenseDistributionCard(
    categorias: List<GastoCategoriaUi>,
    productos: List<VentaProductoUi>,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    // 0 = Gastos, 1 = Productos, 2 = Todas.
    var selectedTab by rememberSaveable {
        mutableStateOf(0)
    }

    // Las claves identifican la categoría o el producto.
    // No dependen de su posición ni del periodo seleccionado.
    val customColors = remember {
        mutableStateMapOf<String, Color>()
    }

    var editingItem by remember {
        mutableStateOf<DistributionItem?>(null)
    }

    val expenseRows = remember(categorias) {
        categorias
            .filter { it.monto > 0L }
            .map { categoria ->
                DistributionItem(
                    key = "expense:${
                        categoria.categoria
                            .trim()
                            .lowercase(Locale.ROOT)
                    }",
                    name = categoria.categoria,
                    amount = categoria.monto
                )
            }
            .sortedWith(
                compareByDescending<DistributionItem> { it.amount }
                    .thenBy { it.name }
            )
    }

    val productRows = remember(productos) {
        productos
            .filter { it.monto > 0L }
            .map { producto ->
                DistributionItem(
                    key = "product:${producto.id}",
                    name = producto.nombre,
                    amount = producto.monto
                )
            }
            .sortedWith(
                compareByDescending<DistributionItem> { it.amount }
                    .thenBy { it.name }
            )
    }

    editingItem?.let { item ->
        DistributionColorDialog(
            itemName = item.name,
            selectedColor = customColors[item.key]
                ?: if (item.key.startsWith("expense:")) {
                    DefaultExpenseColor
                } else {
                    DefaultProductColor
                },
            onChooseColor = { color ->
                customColors[item.key] = color
                editingItem = null
            },
            onReset = {
                customColors.remove(item.key)
                editingItem = null
            },
            onDismiss = {
                editingItem = null
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (selectedTab) {
                    0 -> "Distribución de gastos"
                    1 -> "Ventas por producto o servicio"
                    else -> "Distribución del periodo"
                },
                color = DistributionTitleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Gastos", "Productos", "Todas")
                    .forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                            },
                            label = {
                                Text(label)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DistributionTitleColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF7860FF),
                            strokeWidth = 2.dp
                        )
                    }
                }

                errorMessage != null -> {
                    Text(
                        text = "No se pudo cargar la distribución.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                else -> {
                    if (selectedTab == 0 || selectedTab == 2) {
                        if (selectedTab == 2) {
                            DistributionSectionTitle("Gastos")
                        }

                        DistributionRows(
                            rows = expenseRows,
                            customColors = customColors,
                            defaultColor = DefaultExpenseColor,
                            emptyMessage =
                                "Sin gastos registrados en este periodo.",
                            onChangeColor = { item ->
                                editingItem = item
                            }
                        )
                    }

                    if (selectedTab == 2) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    if (selectedTab == 1 || selectedTab == 2) {
                        if (selectedTab == 2) {
                            DistributionSectionTitle(
                                "Ventas por producto o servicio"
                            )
                        }

                        DistributionRows(
                            rows = productRows,
                            customColors = customColors,
                            defaultColor = DefaultProductColor,
                            emptyMessage =
                                "Sin ventas registradas en este periodo.",
                            onChangeColor = { item ->
                                editingItem = item
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DistributionSectionTitle(text: String) {
    Text(
        text = text,
        color = DistributionTitleColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun DistributionRows(
    rows: List<DistributionItem>,
    customColors: Map<String, Color>,
    defaultColor: Color,
    emptyMessage: String,
    onChangeColor: (DistributionItem) -> Unit
) {
    val total = remember(rows) {
        rows.sumOf { it.amount }
    }

    if (total <= 0L) {
        Text(
            text = emptyMessage,
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEachIndexed { index, item ->
            DistributionItemRow(
                item = item,
                total = total,
                color = customColors[item.key] ?: defaultColor,
                onColorClick = {
                    onChangeColor(item)
                }
            )

            if (index < rows.lastIndex) {
                HorizontalDivider(
                    color = Color(0xFFF1F5F9),
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
private fun DistributionItemRow(
    item: DistributionItem,
    total: Long,
    color: Color,
    onColorClick: () -> Unit
) {
    val fraction = if (total > 0L) {
        (item.amount.toDouble() / total.toDouble())
            .coerceIn(0.0, 1.0)
    } else {
        0.0
    }

    val percentage = remember(fraction) {
        val formatter = NumberFormat.getNumberInstance(
            Locale.forLanguageTag("es-CL")
        ).apply {
            maximumFractionDigits = 1
        }

        if (fraction > 0.0 && fraction * 100 < 0.1) {
            "<0,1%"
        } else {
            "${formatter.format(fraction * 100)}%"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Área de toque amplia, aunque el cuadrito sea pequeño.
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(
                    onClickLabel = "Cambiar color de ${item.name}",
                    onClick = onColorClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Fondo: 100 %. Parte coloreada: porcentaje de la fila.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFF0F2F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.toFloat())
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(color)
                )
            }

            Text(
                text = item.name,
                color = Color(0xFF475569),
                fontSize = 12.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = percentage,
                color = DistributionTitleColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = formatClp(item.amount),
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun DistributionColorDialog(
    itemName: String,
    selectedColor: Color,
    onChooseColor: (Color) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Elegir color")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = itemName,
                    color = Color(0xFF475569),
                    fontSize = 14.sp
                )

                DistributionColorOptions.chunked(3).forEach { options ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        options.forEach { option ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(option.color)
                                    .clickable(
                                        onClickLabel =
                                            "Elegir color ${option.name}"
                                    ) {
                                        onChooseColor(option.color)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == option.color) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text("Restablecer")
            }
        }
    )
}