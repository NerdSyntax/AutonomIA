package com.nerdsyntax.juntalucas.feature.dashboard.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class DashboardPeriodFilter(val label: String) { WEEK("Semana"), MONTH("Mes actual"), CUSTOM("Personalizado") }
data class DashboardDateRange(val start: String, val endExclusive: String)

object DashboardPeriods {
    private val zone = TimeZone.getTimeZone("America/Santiago")
    private fun formatter() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false; timeZone = zone }
    fun today(): String = formatter().format(java.util.Date())
    fun currentMonth(): String = today().take(7)
    fun monthRange(): DashboardDateRange {
        val start = "${currentMonth()}-01"; val calendar = Calendar.getInstance(zone); calendar.time = formatter().parse(start)!!; calendar.add(Calendar.MONTH, 1)
        return DashboardDateRange(start, formatter().format(calendar.time))
    }
    fun weekRange(): DashboardDateRange {
        val calendar = Calendar.getInstance(zone); calendar.time = formatter().parse(today())!!; calendar.firstDayOfWeek = Calendar.MONDAY; calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val start = formatter().format(calendar.time); calendar.add(Calendar.DAY_OF_MONTH, 7)
        return DashboardDateRange(start, formatter().format(calendar.time))
    }
    fun customRange(start: String, endInclusive: String): DashboardDateRange {
        val calendar = Calendar.getInstance(zone); calendar.time = formatter().parse(endInclusive)!!; calendar.add(Calendar.DAY_OF_MONTH, 1)
        return DashboardDateRange(start, formatter().format(calendar.time))
    }
    fun valid(date: String): Boolean = runCatching { date.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) && formatter().parse(date) != null }.getOrDefault(false)
}
