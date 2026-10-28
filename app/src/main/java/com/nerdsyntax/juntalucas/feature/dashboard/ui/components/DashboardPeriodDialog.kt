package com.nerdsyntax.juntalucas.feature.dashboard.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nerdsyntax.juntalucas.feature.dashboard.domain.DashboardPeriodFilter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardPeriodDialog(
    selectedFilter: DashboardPeriodFilter,
    initialStart: String,
    initialEnd: String,
    onDismiss: () -> Unit,
    onApply: (DashboardPeriodFilter, String, String) -> Unit
) {
    val context = LocalContext.current
    val locale = remember { Locale.forLanguageTag("es-CL") }

    val today = remember {
        LocalDate.now(ZoneId.of("America/Santiago"))
    }

    val firstDate = remember(initialStart) {
        runCatching {
            LocalDate.parse(initialStart)
        }.getOrDefault(today)
    }

    val lastDate = remember(initialEnd, firstDate) {
        runCatching {
            LocalDate.parse(initialEnd)
        }.getOrDefault(firstDate)
    }

    var mode by remember { mutableStateOf(selectedFilter) }
    var selectedYear by remember { mutableStateOf(firstDate.year) }
    var selectedMonth by remember { mutableStateOf(firstDate.monthValue) }
    var startDate by remember { mutableStateOf(firstDate) }
    var endDate by remember { mutableStateOf(lastDate) }

    var yearMenuExpanded by remember { mutableStateOf(false) }
    var monthMenuExpanded by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    val monthFormatter = remember(locale) {
        DateTimeFormatter.ofPattern("MMMM", locale)
    }

    // Mantiene una lista estable mientras el diálogo está abierto.
    val availableYears = remember(today, firstDate) {
        (maxOf(today.year, firstDate.year) downTo 1900).toList()
    }

    fun openDatePicker(
        current: LocalDate,
        onSelected: (LocalDate) -> Unit
    ) {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                onSelected(LocalDate.of(year, month + 1, day))
            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    val weekStart = startDate.minusDays(
        (startDate.dayOfWeek.value - 1).toLong()
    )
    val weekEnd = weekStart.plusDays(6)

    val validSelection =
        mode != DashboardPeriodFilter.CUSTOM ||
                !endDate.isBefore(startDate)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (mode) {
                    DashboardPeriodFilter.WEEK -> "Seleccionar semana"
                    DashboardPeriodFilter.MONTH -> "Seleccionar mes"
                    DashboardPeriodFilter.YEAR -> "Seleccionar año"
                    DashboardPeriodFilter.CUSTOM -> "Rango personalizado"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (
                    mode == DashboardPeriodFilter.MONTH ||
                    mode == DashboardPeriodFilter.YEAR
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { yearMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Año: $selectedYear ▾")
                        }

                        DropdownMenu(
                            expanded = yearMenuExpanded,
                            onDismissRequest = {
                                yearMenuExpanded = false
                            },
                            modifier = Modifier.heightIn(max = 280.dp)
                        ) {
                            availableYears.forEach { year ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (year == selectedYear) {
                                                "$year ✓"
                                            } else {
                                                year.toString()
                                            }
                                        )
                                    },
                                    onClick = {
                                        selectedYear = year
                                        yearMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (mode == DashboardPeriodFilter.MONTH) {
                    val monthName = LocalDate
                        .of(selectedYear, selectedMonth, 1)
                        .format(monthFormatter)
                        .replaceFirstChar { it.titlecase(locale) }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { monthMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mes: $monthName ▾")
                        }

                        DropdownMenu(
                            expanded = monthMenuExpanded,
                            onDismissRequest = {
                                monthMenuExpanded = false
                            },
                            modifier = Modifier.heightIn(max = 280.dp)
                        ) {
                            (1..12).forEach { month ->
                                val name = LocalDate
                                    .of(selectedYear, month, 1)
                                    .format(monthFormatter)
                                    .replaceFirstChar {
                                        it.titlecase(locale)
                                    }

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (month == selectedMonth) {
                                                "$name ✓"
                                            } else {
                                                name
                                            }
                                        )
                                    },
                                    onClick = {
                                        selectedMonth = month
                                        monthMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (mode == DashboardPeriodFilter.WEEK) {
                    Text("Elige un día de la semana que quieres consultar.")

                    OutlinedButton(
                        onClick = {
                            openDatePicker(startDate) {
                                startDate = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(startDate.format(dateFormatter))
                    }

                    Text(
                        text = "${weekStart.format(dateFormatter)}" +
                                " — ${weekEnd.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (mode == DashboardPeriodFilter.CUSTOM) {
                    OutlinedButton(
                        onClick = {
                            openDatePicker(startDate) {
                                startDate = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Desde: ${startDate.format(dateFormatter)}")
                    }

                    OutlinedButton(
                        onClick = {
                            openDatePicker(endDate) {
                                endDate = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hasta: ${endDate.format(dateFormatter)}")
                    }

                    if (!validSelection) {
                        Text(
                            text = "La fecha final debe ser igual o posterior a la inicial.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (mode != DashboardPeriodFilter.CUSTOM) {
                    TextButton(
                        onClick = {
                            // Usa como punto de partida el periodo
                            // que estabas seleccionando.
                            val first = when (mode) {
                                DashboardPeriodFilter.WEEK -> weekStart

                                DashboardPeriodFilter.MONTH ->
                                    LocalDate.of(
                                        selectedYear,
                                        selectedMonth,
                                        1
                                    )

                                DashboardPeriodFilter.YEAR ->
                                    LocalDate.of(selectedYear, 1, 1)

                                DashboardPeriodFilter.CUSTOM -> startDate
                            }

                            val last = when (mode) {
                                DashboardPeriodFilter.WEEK ->
                                    first.plusDays(6)

                                DashboardPeriodFilter.MONTH ->
                                    first.plusMonths(1).minusDays(1)

                                DashboardPeriodFilter.YEAR ->
                                    first.plusYears(1).minusDays(1)

                                DashboardPeriodFilter.CUSTOM -> endDate
                            }

                            startDate = first
                            endDate = last
                            mode = DashboardPeriodFilter.CUSTOM
                        }
                    ) {
                        Text("Elegir rango personalizado")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = validSelection,
                onClick = {
                    val first = when (mode) {
                        DashboardPeriodFilter.WEEK -> weekStart

                        DashboardPeriodFilter.MONTH ->
                            LocalDate.of(selectedYear, selectedMonth, 1)

                        DashboardPeriodFilter.YEAR ->
                            LocalDate.of(selectedYear, 1, 1)

                        DashboardPeriodFilter.CUSTOM -> startDate
                    }

                    val last = when (mode) {
                        DashboardPeriodFilter.WEEK -> first.plusDays(6)

                        DashboardPeriodFilter.MONTH ->
                            first.plusMonths(1).minusDays(1)

                        DashboardPeriodFilter.YEAR ->
                            first.plusYears(1).minusDays(1)

                        DashboardPeriodFilter.CUSTOM -> endDate
                    }

                    onApply(
                        mode,
                        first.toString(),
                        last.toString()
                    )
                }
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}