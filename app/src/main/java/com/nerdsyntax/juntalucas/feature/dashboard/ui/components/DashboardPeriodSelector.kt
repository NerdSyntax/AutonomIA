package com.nerdsyntax.juntalucas.feature.dashboard.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nerdsyntax.juntalucas.feature.dashboard.domain.DashboardPeriodFilter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PeriodNavy = Color(0xFF0F2A4A)
private val PeriodGray = Color(0xFFE7ECF1)

@Composable
fun DashboardPeriodSelector(
    selectedFilter: DashboardPeriodFilter,
    dateLabel: String,
    initialStart: String,
    initialEnd: String,
    onFilterSelected: (DashboardPeriodFilter) -> Unit,
    onPeriodApplied: (DashboardPeriodFilter, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locale = remember { Locale.forLanguageTag("es-CL") }

    val today = LocalDate.now(
        ZoneId.of("America/Santiago")
    )

    val anchor = runCatching {
        LocalDate.parse(initialStart)
    }.getOrDefault(today)

    val selectedEnd = runCatching {
        LocalDate.parse(initialEnd)
    }.getOrDefault(anchor)

    var expanded by remember { mutableStateOf(false) }
    var menuYear by remember { mutableStateOf(today.year) }

    val buttonLabel = when (selectedFilter) {
        DashboardPeriodFilter.WEEK -> {
            val monday = anchor.minusDays(
                (anchor.dayOfWeek.value - 1).toLong()
            )

            val sunday = monday.plusDays(6)

            if (
                monday.month == sunday.month &&
                monday.year == sunday.year
            ) {
                val month = sunday.format(
                    DateTimeFormatter.ofPattern("MMM", locale)
                ).replace(".", "")

                "${monday.dayOfMonth}–${sunday.dayOfMonth} $month"
            } else {
                val formatter = DateTimeFormatter.ofPattern("d/M")

                "${monday.format(formatter)}–${sunday.format(formatter)}"
            }
        }

        DashboardPeriodFilter.MONTH -> {
            anchor.format(
                DateTimeFormatter.ofPattern("MMM yyyy", locale)
            ).replace(".", "").replaceFirstChar {
                it.titlecase(locale)
            }
        }

        DashboardPeriodFilter.YEAR -> anchor.year.toString()

        DashboardPeriodFilter.CUSTOM -> "Personalizado"
    }

    fun applyPeriod(
        filter: DashboardPeriodFilter,
        start: LocalDate,
        end: LocalDate
    ) {
        expanded = false

        onPeriodApplied(
            filter,
            start.toString(),
            end.toString()
        )
    }

    fun applyWeek(day: LocalDate) {
        val monday = day.minusDays(
            (day.dayOfWeek.value - 1).toLong()
        )

        applyPeriod(
            DashboardPeriodFilter.WEEK,
            monday,
            monday.plusDays(6)
        )
    }

    fun openDatePicker(
        current: LocalDate,
        title: String,
        minimumDate: LocalDate? = null,
        onSelected: (LocalDate) -> Unit
    ) {
        expanded = false

        val picker = DatePickerDialog(
            context,
            { _, year, month, day ->
                onSelected(
                    LocalDate.of(year, month + 1, day)
                )
            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        )

        picker.setTitle(title)

        if (minimumDate != null) {
            picker.datePicker.minDate = minimumDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        picker.setOnCancelListener {
            // Conserva el periodo anterior al cancelar.
            expanded = true
        }

        picker.show()
    }

    fun openCustomRange() {
        openDatePicker(
            current = anchor,
            title = "1 de 2 · Fecha inicial"
        ) { firstDate ->
            val suggestedEnd = if (selectedEnd.isBefore(firstDate)) {
                firstDate
            } else {
                selectedEnd
            }

            openDatePicker(
                current = suggestedEnd,
                title = "2 de 2 · Fecha final",
                minimumDate = firstDate
            ) { lastDate ->
                applyPeriod(
                    DashboardPeriodFilter.CUSTOM,
                    firstDate,
                    lastDate
                )
            }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            color = PeriodGray,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val tabs = listOf(
                    DashboardPeriodFilter.WEEK to "Semana",
                    DashboardPeriodFilter.MONTH to "Mes",
                    DashboardPeriodFilter.YEAR to "Año"
                )

                tabs.forEach { (filter, label) ->
                    val selected = selectedFilter == filter

                    Surface(
                        onClick = {
                            expanded = false
                            onFilterSelected(filter)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) {
                            Color.White
                        } else {
                            Color.Transparent
                        },
                        shadowElevation = if (selected) 1.dp else 0.dp
                    ) {
                        Box(
                            modifier = Modifier.heightIn(min = 44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) {
                                    PeriodNavy
                                } else {
                                    Color(0xFF64748B)
                                },
                                fontSize = 11.sp,
                                fontWeight = if (selected) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Box {
            Surface(
                onClick = {
                    when {
                        selectedFilter == DashboardPeriodFilter.CUSTOM -> {
                            openCustomRange()
                        }

                        expanded -> {
                            expanded = false
                        }

                        else -> {
                            menuYear = anchor.year
                            expanded = true
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(
                    1.dp,
                    Color(0xFFE2E8F0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(min = 112.dp, max = 144.dp)
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = PeriodNavy,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = buttonLabel.ifBlank { dateLabel },
                        modifier = Modifier.weight(1f),
                        color = PeriodNavy,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Seleccionar periodo",
                        tint = PeriodNavy,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(280.dp),
                containerColor = Color.White
            ) {
                when (selectedFilter) {
                    DashboardPeriodFilter.WEEK -> {
                        DropdownMenuItem(
                            text = {
                                Text("Esta semana")
                            },
                            onClick = {
                                applyWeek(today)
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Semana pasada")
                            },
                            onClick = {
                                applyWeek(today.minusWeeks(1))
                            }
                        )
                    }

                    DashboardPeriodFilter.MONTH -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                enabled = menuYear > 1900,
                                onClick = { menuYear-- }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Año anterior",
                                    tint = PeriodNavy
                                )
                            }

                            Text(
                                text = menuYear.toString(),
                                color = PeriodNavy,
                                fontWeight = FontWeight.SemiBold
                            )

                            IconButton(
                                enabled = menuYear < 9998,
                                onClick = { menuYear++ }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Año siguiente",
                                    tint = PeriodNavy
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            (1..12).forEach { month ->
                                val first = LocalDate.of(
                                    menuYear,
                                    month,
                                    1
                                )

                                val name = first.format(
                                    DateTimeFormatter.ofPattern(
                                        "MMMM",
                                        locale
                                    )
                                ).replaceFirstChar {
                                    it.titlecase(locale)
                                }

                                val selected =
                                    menuYear == anchor.year &&
                                            month == anchor.monthValue

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (selected) {
                                                "$name ✓"
                                            } else {
                                                name
                                            }
                                        )
                                    },
                                    onClick = {
                                        applyPeriod(
                                            DashboardPeriodFilter.MONTH,
                                            first,
                                            first.plusMonths(1).minusDays(1)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    DashboardPeriodFilter.YEAR -> {
                        val latestYear = maxOf(
                            today.year,
                            anchor.year
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            (latestYear downTo 1900).forEach { year ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (year == anchor.year) {
                                                "$year ✓"
                                            } else {
                                                year.toString()
                                            }
                                        )
                                    },
                                    onClick = {
                                        applyPeriod(
                                            DashboardPeriodFilter.YEAR,
                                            LocalDate.of(year, 1, 1),
                                            LocalDate.of(year, 12, 31)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    DashboardPeriodFilter.CUSTOM -> Unit
                }

                if (selectedFilter != DashboardPeriodFilter.CUSTOM) {
                    HorizontalDivider(
                        color = Color(0xFFE2E8F0)
                    )
                }

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Personalizado",
                            color = PeriodNavy,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = PeriodNavy
                        )
                    },
                    onClick = {
                        when (selectedFilter) {
                            DashboardPeriodFilter.WEEK -> {
                                openDatePicker(
                                    current = anchor,
                                    title = "Elige un día de la semana que quieres consultar"
                                ) { selected ->
                                    applyWeek(selected)
                                }
                            }

                            DashboardPeriodFilter.MONTH,
                            DashboardPeriodFilter.YEAR,
                            DashboardPeriodFilter.CUSTOM -> {
                                openCustomRange()
                            }
                        }
                    }
                )
            }
        }
    }
}