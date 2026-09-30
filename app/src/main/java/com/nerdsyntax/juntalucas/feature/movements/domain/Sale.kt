package com.nerdsyntax.juntalucas.feature.movements.domain

import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.RemoteData
import com.nerdsyntax.juntalucas.core.format.MAX_MONEY
import com.nerdsyntax.juntalucas.core.format.MAX_QUANTITY
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class PaymentMethod(val label: String) {
    CASH("Efectivo"), DEBIT("Débito"), CREDIT("Crédito"), TRANSFER("Transferencia")
}

data class SaleDraft(
    val id: String,
    val userId: String,
    val saleDate: String,
    val productId: String,
    val quantity: Long,
    val unitPrice: Long,
    val discount: Long,
    val paymentMethod: PaymentMethod,
    val note: String = ""
) {
    val total: Long get() = calculateSaleTotal(quantity, unitPrice, discount)
    fun validate() {
        if (id.isBlank() || userId.isBlank() || productId.isBlank() || '/' in id || '/' in productId)
            throw DataValidationException("Selecciona un producto o servicio válido.")
        if (!SaleDates.valid(saleDate)) throw DataValidationException("Selecciona una fecha válida.")
        if (note.length > 2000) throw DataValidationException("La nota admite hasta 2000 caracteres.")
        total
    }
}

data class Sale(
    val draft: SaleDraft,
    val productName: String,
    val unitCost: Long?,
    val createdAtMillis: Long,
    val stockTracked: Boolean
)

interface SalesRepository {
    fun observeMonth(uid: String, month: String): Flow<Result<RemoteData<List<Sale>>>>
    fun observeRange(uid: String, startDate: String, endDateExclusive: String): Flow<Result<RemoteData<List<Sale>>>> = observeMonth(uid, startDate.take(7))
    suspend fun save(draft: SaleDraft): Result<Unit>
}

fun calculateSaleTotal(quantity: Long, unitPrice: Long, discount: Long): Long {
    if (quantity !in 1..MAX_QUANTITY) throw DataValidationException("Ingresa una cantidad entera positiva (máximo 1.000.000).")
    if (unitPrice !in 1..MAX_MONEY) throw DataValidationException("Ingresa un precio positivo en pesos enteros.")
    // Division avoids overflow before multiplication.
    if (unitPrice > MAX_MONEY / quantity) throw DataValidationException("El subtotal supera el máximo permitido.")
    val subtotal = quantity * unitPrice
    if (discount !in 0..subtotal) throw DataValidationException("El descuento no puede superar el subtotal.")
    return subtotal - discount
}

data class SalesSummary(val total: Long, val average: Long)

fun summarizeSales(sales: List<Sale>): SalesSummary {
    val total = sales.fold(0L) { sum, sale -> Math.addExact(sum, sale.draft.total) }
    if (sales.isEmpty()) return SalesSummary(0, 0)
    val count = sales.size.toLong()
    // Ticket rounded to the nearest whole peso, without floating point.
    return SalesSummary(total, total / count + if ((total % count) * 2 >= count) 1 else 0)
}

/** Civil dates (no time of day); month boundaries do not depend on DST or UTC conversion. */
object SaleDates {
    private fun format(pattern: String) = SimpleDateFormat(pattern, Locale.forLanguageTag("es-CL")).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }
    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(java.util.Date())
    fun valid(date: String): Boolean = runCatching {
        date.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) &&
            date.substring(0, 4).toInt() in 1900..9999 && format("yyyy-MM-dd").parse(date) != null
    }.getOrDefault(false)
    fun validMonth(month: String): Boolean = valid("$month-01")
    fun shiftMonth(month: String, offset: Int): String {
        require(validMonth(month))
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = format("yyyy-MM-dd").parse("$month-01")!!
        calendar.add(Calendar.MONTH, offset)
        return format("yyyy-MM").format(calendar.time)
    }
    fun monthLabel(month: String): String = format("MMMM yyyy").format(format("yyyy-MM-dd").parse("$month-01")!!)
    fun display(date: String): String = format("dd-MM-yyyy").format(format("yyyy-MM-dd").parse(date)!!)
}
