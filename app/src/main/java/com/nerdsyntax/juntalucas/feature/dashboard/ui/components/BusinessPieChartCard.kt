package com.nerdsyntax.juntalucas.feature.dashboard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nerdsyntax.juntalucas.core.format.formatClp
import com.nerdsyntax.juntalucas.feature.dashboard.ui.GastoCategoriaUi
import com.nerdsyntax.juntalucas.feature.dashboard.ui.VentaProductoUi
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

private val PieNavy = Color(0xFF0F2A4A)

private val PieColors = listOf(
    Color(0xFF6366F1),
    Color(0xFF059669),
    Color(0xFFDC2626),
    Color(0xFFF59E0B),
    Color(0xFF7C3AED),
    Color(0xFF0284C7),
    Color(0xFFDB2777),
    Color(0xFF0D9488)
)

private data class PieRow(
    val id: String,
    val name: String,
    val amount: Long
)

@Composable
fun BusinessPieChartCard(
    categorias: List<GastoCategoriaUi>,
    productos: List<VentaProductoUi>,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var selectedTab by rememberSaveable {
        mutableStateOf(0)
    }

    var selectedId by remember(selectedTab) {
        mutableStateOf<String?>(null)
    }

    val rows = remember(categorias, productos, selectedTab) {
        val data = if (selectedTab == 0) {
            categorias.map {
                PieRow(
                    id = "gasto:${it.categoria.trim().lowercase(Locale.ROOT)}",
                    name = it.categoria,
                    amount = it.monto
                )
            }
        } else {
            productos.map {
                PieRow(
                    id = "producto:${it.id}",
                    name = it.nombre,
                    amount = it.monto
                )
            }
        }

        data.filter { it.amount > 0L }
            .sortedWith(
                compareByDescending<PieRow> { it.amount }
                    .thenBy { it.id }
            )
    }

    val total = remember(rows) {
        rows.fold(BigDecimal.ZERO) { sum, row ->
            sum + row.amount.toBigDecimal()
        }
    }

    val fractions = remember(rows, total) {
        rows.map { row ->
            row.amount.toBigDecimal()
                .divide(total, 16, RoundingMode.HALF_UP)
                .toDouble()
        }
    }

    val selected = rows.firstOrNull {
        it.id == selectedId
    }

    val amountFormatter = remember {
        NumberFormat.getIntegerInstance(
            Locale.forLanguageTag("es-CL")
        )
    }

    val percentageFormatter = remember {
        NumberFormat.getPercentInstance(
            Locale.forLanguageTag("es-CL")
        ).apply {
            maximumFractionDigits = 1
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Distribución del negocio",
                color = PieNavy,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Gastos", "Ventas por producto")
                    .forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                            },
                            label = {
                                Text(label)
                            }
                        )
                    }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF7C3AED)
                        )
                    }
                }

                errorMessage != null -> {
                    Text(
                        text = "No se pudo cargar la distribución.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                rows.isEmpty() -> {
                    Text(
                        text = if (selectedTab == 0) {
                            "Sin gastos en el periodo seleccionado."
                        } else {
                            "Sin ventas en el periodo seleccionado."
                        },
                        modifier = Modifier.padding(vertical = 24.dp),
                        color = Color(0xFF64748B)
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val strokeWidth = 40.dp.toPx()

                            val radius = (
                                    size.minDimension / 2f -
                                            strokeWidth / 2f -
                                            8.dp.toPx()
                                    ).coerceAtLeast(1f)

                            val topLeft = Offset(
                                x = center.x - radius,
                                y = center.y - radius
                            )

                            val arcSize = Size(
                                width = radius * 2f,
                                height = radius * 2f
                            )

                            var startAngle = -90f

                            rows.forEachIndexed { index, row ->
                                val sweepAngle =
                                    if (index == rows.lastIndex) {
                                        270f - startAngle
                                    } else {
                                        (fractions[index] * 360.0)
                                            .toFloat()
                                    }

                                val color = colorFor(row.id)

                                drawArc(
                                    color = color.copy(
                                        alpha = if (
                                            selected == null ||
                                            selected.id == row.id
                                        ) {
                                            1f
                                        } else {
                                            0.3f
                                        }
                                    ),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(
                                        width = strokeWidth
                                    )
                                )

                                startAngle += sweepAngle
                            }
                        }

                        Column(
                            modifier = Modifier.widthIn(max = 140.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (selected == null) {
                                    "Total"
                                } else {
                                    selected.name
                                },
                                textAlign = TextAlign.Center,
                                color = Color(0xFF64748B),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = if (selected == null) {
                                    "$" + amountFormatter.format(total)
                                } else {
                                    formatClp(selected.amount)
                                },
                                textAlign = TextAlign.Center,
                                color = PieNavy,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    rows.forEachIndexed { index, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (selectedId == row.id) {
                                        Color(0xFFF1F5F9)
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedId = if (
                                        selectedId == row.id
                                    ) {
                                        null
                                    } else {
                                        row.id
                                    }
                                }
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 10.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        color = colorFor(row.id),
                                        shape = CircleShape
                                    )
                            )

                            Text(
                                text = row.name,
                                modifier = Modifier.weight(1f),
                                color = PieNavy,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = if (
                                        fractions[index] < 0.001
                                    ) {
                                        "<0,1 %"
                                    } else {
                                        percentageFormatter.format(
                                            fractions[index]
                                        )
                                    },
                                    color = PieNavy,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = formatClp(row.amount),
                                    color = Color(0xFF64748B),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun colorFor(id: String): Color {
    return PieColors[
        Math.floorMod(id.hashCode(), PieColors.size)
    ]
}