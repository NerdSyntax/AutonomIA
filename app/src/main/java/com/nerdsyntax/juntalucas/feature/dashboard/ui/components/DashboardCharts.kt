package com.nerdsyntax.juntalucas.feature.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nerdsyntax.juntalucas.feature.dashboard.ui.DashboardUiState
import com.nerdsyntax.juntalucas.feature.dashboard.ui.MesGraficoUi
import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.data.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.cartesian.marker.*
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.*
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

private val SalesColor = Color(0xFF193B63)
private val ExpensesColor = Color(0xFFA8C5E9)
private val ProfitColor = Color(0xFF7860FF)
private val ChartText = Color(0xFF8492A6)
private val GridColor = Color(0xFFEDF1F5)
private val ChartLocale = Locale.forLanguageTag("es-CL")

@Composable
fun DashboardCharts(
    state: DashboardUiState,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when {
            state.graficosCargando -> {
                ChartCard(title = "Cargando gráficos") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            color = ProfitColor,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            state.errorGraficos != null -> {
                ChartCard(title = "Gráficos") {
                    Text(
                        text = state.errorGraficos.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )

                    TextButton(onClick = onRetry) {
                        Text("Reintentar")
                    }
                }
            }

            state.mesesGrafico.isEmpty() -> {
                ChartCard(title = "Ventas vs. gastos") {
                    EmptyChart("Sin movimientos en este periodo.")
                }
            }

            else -> {
                if (state.graficosDesdeCache) {
                    Text(
                        text = "Datos guardados en el dispositivo",
                        color = ChartText,
                        fontSize = 11.sp
                    )
                }

                key(state.periodLabel, state.mesesGrafico) {
                    SalesExpensesChart(state.mesesGrafico)
                    ProfitChart(state.mesesGrafico)
                }
            }
        }
    }
}

@Composable
private fun SalesExpensesChart(datos: List<MesGraficoUi>) {
    val model = remember(datos) {
        CartesianChartModel(
            ColumnCartesianLayerModel.build {
                series(
                    x = datos.indices.toList(),
                    y = datos.map { it.ventas }
                )
                series(
                    x = datos.indices.toList(),
                    y = datos.map { it.gastos }
                )
            }
        )
    }

    val salesColumn = rememberLineComponent(
        fill = Fill(SalesColor),
        thickness = 13.dp,
        shape = RoundedCornerShape(
            topStart = 3.dp,
            topEnd = 3.dp
        )
    )

    val expensesColumn = rememberLineComponent(
        fill = Fill(ExpensesColor),
        thickness = 13.dp,
        shape = RoundedCornerShape(
            topStart = 3.dp,
            topEnd = 3.dp
        )
    )

    val dateFormatter = chartDateFormatter(datos)
    val moneyFormatter = moneyAxisFormatter()
    val marker = dataMarker(datos, profit = false)

    val label = rememberTextComponent(
        style = TextStyle(
            color = ChartText,
            fontSize = 10.sp
        ),
        padding = Insets(horizontal = 3.dp, vertical = 4.dp)
    )

    val guideline = rememberLineComponent(
        fill = Fill(GridColor),
        thickness = 1.dp
    )

    val dateSpacing = chartLabelSpacing(datos.size)

    ChartCard(
        title = "Ventas vs. gastos",
        legend = true
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider =
                        ColumnCartesianLayer.ColumnProvider.series(
                            salesColumn,
                            expensesColumn
                        )
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = label,
                    valueFormatter = moneyFormatter,
                    line = null,
                    tick = null,
                    guideline = guideline,
                    itemPlacer = remember {
                        VerticalAxis.ItemPlacer.count(count = { 4 })
                    }
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = label,
                    valueFormatter = dateFormatter,
                    guideline = null,
                    line = null,
                    tick = null,
                    itemPlacer = remember(dateSpacing) {
                        HorizontalAxis.ItemPlacer.aligned(
                            spacing = { dateSpacing },
                            addExtremeLabelPadding = true
                        )
                    }
                ),
                marker = marker
            ),
            model = model,
            modifier = Modifier.fillMaxWidth(),
            chartAreaHeight = 128.dp,
            zoomState = rememberVicoZoomState(
                initialZoom = Zoom.Content
            )
        )
    }
}

@Composable
private fun ProfitChart(datos: List<MesGraficoUi>) {
    // Cada tramo contiene únicamente valores calculables.
    // Un costo desconocido nunca se convierte en ganancia cero.
    val segments = remember(datos) {
        val result = mutableListOf<List<Pair<Int, Double>>>()
        var current = mutableListOf<Pair<Int, Double>>()

        datos.forEachIndexed { index, item ->
            val value = item.gananciaBruta

            if (value == null || !value.isFinite()) {
                if (current.isNotEmpty()) {
                    result.add(current.toList())
                    current = mutableListOf()
                }
            } else {
                current.add(index to value)
            }
        }

        if (current.isNotEmpty()) {
            result.add(current.toList())
        }

        result
    }

    ChartCard(title = "Evolución de ganancia bruta") {
        if (segments.isEmpty()) {
            EmptyChart(
                message = if (datos.any { it.tieneVentas }) {
                    "Faltan costos para calcular la ganancia."
                } else {
                    "Sin ventas en este periodo."
                }
            )
        } else {
            val model = remember(segments) {
                CartesianChartModel(
                    LineCartesianLayerModel.build {
                        segments.forEach { segment ->
                            series(
                                x = segment.map { it.first },
                                y = segment.map { it.second }
                            )
                        }
                    }
                )
            }

            val point = rememberShapeComponent(
                fill = Fill(ProfitColor),
                shape = CircleShape
            )

            val line = LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(
                    Fill(ProfitColor)
                ),
                pointProvider =
                    LineCartesianLayer.PointProvider.single(
                        LineCartesianLayer.Point(point)
                    )
            )

            val label = rememberTextComponent(
                style = TextStyle(
                    color = ChartText,
                    fontSize = 10.sp
                ),
                padding = Insets(
                    horizontal = 3.dp,
                    vertical = 4.dp
                )
            )

            val guideline = rememberLineComponent(
                fill = Fill(GridColor),
                thickness = 1.dp
            )

            val dateFormatter = chartDateFormatter(datos)
            val moneyFormatter = moneyAxisFormatter()
            val marker = dataMarker(datos, profit = true)
            val dateSpacing = chartLabelSpacing(datos.size)

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider =
                            LineCartesianLayer.LineProvider.series(line)
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        label = label,
                        valueFormatter = moneyFormatter,
                        line = null,
                        tick = null,
                        guideline = guideline,
                        itemPlacer = remember {
                            VerticalAxis.ItemPlacer.count(
                                count = { 4 }
                            )
                        }
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = label,
                        valueFormatter = dateFormatter,
                        guideline = null,
                        line = null,
                        tick = null,
                        itemPlacer = remember(dateSpacing) {
                            HorizontalAxis.ItemPlacer.aligned(
                                spacing = { dateSpacing },
                                addExtremeLabelPadding = true
                            )
                        }
                    ),
                    marker = marker
                ),
                model = model,
                modifier = Modifier.fillMaxWidth(),
                chartAreaHeight = 128.dp,
                zoomState = rememberVicoZoomState(
                    initialZoom = Zoom.Content
                )
            )
        }
    }
}

@Composable
private fun dataMarker(
    datos: List<MesGraficoUi>,
    profit: Boolean
): DefaultCartesianMarker {
    val formatter = remember(datos, profit) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            val index = targets.firstOrNull()?.x?.roundToInt()
            val item = index?.let { datos.getOrNull(it) }

            if (item == null) {
                ""
            } else {
                val date = chartDate(
                    periodo = item.periodo,
                    fallback = item.etiqueta,
                    full = true
                )

                if (profit) {
                    val amount = item.gananciaBruta?.let {
                        chartMoney(it)
                    } ?: "No disponible"

                    "$date\nGanancia bruta: $amount"
                } else {
                    "$date\n" +
                            "Ventas: ${chartMoney(item.ventas)}\n" +
                            "Gastos: ${chartMoney(item.gastos)}"
                }
            }
        }
    }

    return rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            style = TextStyle(
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            ),
            lineCount = if (profit) 2 else 3,
            padding = Insets(
                horizontal = 10.dp,
                vertical = 8.dp
            ),
            background = rememberShapeComponent(
                fill = Fill(SalesColor),
                shape = RoundedCornerShape(10.dp)
            )
        ),
        valueFormatter = formatter,
        labelPosition =
            DefaultCartesianMarker.LabelPosition.AroundPoint
    )
}

@Composable
private fun chartDateFormatter(
    datos: List<MesGraficoUi>
): CartesianValueFormatter {
    return remember(datos) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.roundToInt()

            // Evita repetir una fecha para posiciones fraccionarias.
            if (abs(value - index.toDouble()) > 0.001) {
                ""
            } else {
                datos.getOrNull(index)?.let { item ->
                    chartDate(
                        periodo = item.periodo,
                        fallback = item.etiqueta,
                        full = false
                    )
                }.orEmpty()
            }
        }
    }
}

private fun chartLabelSpacing(size: Int): Int {
    // Hasta cinco etiquetas, conservando todos los puntos reales.
    return ceil(size / 5.0).toInt().coerceAtLeast(1)
}

private fun chartDate(
    periodo: String,
    fallback: String,
    full: Boolean
): String {
    return runCatching {
        when (periodo.length) {
            10 -> {
                LocalDate.parse(periodo).format(
                    DateTimeFormatter.ofPattern(
                        if (full) "d 'de' MMMM 'de' yyyy" else "dd MMM",
                        ChartLocale
                    )
                )
            }

            7 -> {
                YearMonth.parse(periodo).format(
                    DateTimeFormatter.ofPattern(
                        if (full) "MMMM yyyy" else "MMM yy",
                        ChartLocale
                    )
                )
            }

            else -> fallback
        }
    }.getOrDefault(fallback)
}

@Composable
private fun moneyAxisFormatter(): CartesianValueFormatter {
    return remember {
        CartesianValueFormatter { _, value, _ ->
            when {
                abs(value) >= 1_000_000 -> {
                    String.format(
                        ChartLocale,
                        "%.1f M",
                        value / 1_000_000
                    )
                }

                abs(value) >= 1_000 -> {
                    NumberFormat.getNumberInstance(ChartLocale).apply {
                        maximumFractionDigits = 1
                    }.format(value / 1_000) + " mil"
                }

                else -> {
                    NumberFormat.getIntegerInstance(ChartLocale)
                        .format(value)
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    legend: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
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
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // La leyenda pasa debajo si la pantalla o el texto
            // son demasiado grandes para caber en una sola fila.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(end = 10.dp),
                    color = SalesColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                if (legend) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ChartLegend("Ventas", SalesColor)
                        ChartLegend("Gastos", ExpensesColor)
                    }
                }
            }

            content()
        }
    }
}

@Composable
private fun ChartLegend(
    title: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Text(
            text = title,
            color = ChartText,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun EmptyChart(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = ChartText,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun chartMoney(value: Number): String {
    return "$" + NumberFormat
        .getIntegerInstance(ChartLocale)
        .format(value)
}