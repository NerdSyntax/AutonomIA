package com.nerdsyntax.juntalucas.feature.dashboard.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class DashboardPeriodFilter(val label: String) {
    WEEK("Semana"),
    MONTH("Mes"),
    YEAR("Año"),
    CUSTOM("Personalizado")
}

data class DashboardDateRange(
    val start: String,
    val endExclusive: String
)

object DashboardPeriods {

    private val zone = ZoneId.of("America/Santiago")

    private fun currentDate(): LocalDate = LocalDate.now(zone)

    fun today(): String = currentDate().toString()

    fun currentMonth(): String =
        YearMonth.from(currentDate()).toString()

    fun currentYear(): Int = currentDate().year

    // Sin argumento usa el mes actual.
    // También permite consultar, por ejemplo, "2026-08".
    fun monthRange(
        month: String = currentMonth()
    ): DashboardDateRange {
        val selectedMonth = YearMonth.parse(month)

        return DashboardDateRange(
            start = selectedMonth.atDay(1).toString(),
            endExclusive = selectedMonth
                .plusMonths(1)
                .atDay(1)
                .toString()
        )
    }

    // Semana de lunes a domingo.
    fun weekRange(): DashboardDateRange {
        val start = currentDate().with(
            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        )

        return DashboardDateRange(
            start = start.toString(),
            endExclusive = start.plusWeeks(1).toString()
        )
    }

    // Sin argumento usa el año actual.
    fun yearRange(
        year: Int = currentYear()
    ): DashboardDateRange {
        val start = LocalDate.of(year, 1, 1)

        return DashboardDateRange(
            start = start.toString(),
            endExclusive = start.plusYears(1).toString()
        )
    }

    // La fecha final que elige el usuario está incluida.
    fun customRange(
        start: String,
        endInclusive: String
    ): DashboardDateRange {
        require(valid(start) && valid(endInclusive)) {
            "Selecciona fechas válidas."
        }

        val firstDay = LocalDate.parse(start)
        val lastDay = LocalDate.parse(endInclusive)

        require(!lastDay.isBefore(firstDay)) {
            "La fecha final no puede ser anterior a la inicial."
        }

        return DashboardDateRange(
            start = firstDay.toString(),
            endExclusive = lastDay.plusDays(1).toString()
        )
    }

    fun valid(date: String): Boolean {
        if (!date.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"))) {
            return false
        }

        return runCatching {
            LocalDate.parse(date)
        }.isSuccess
    }
}