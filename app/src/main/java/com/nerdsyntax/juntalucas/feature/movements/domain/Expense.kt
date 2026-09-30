package com.nerdsyntax.juntalucas.feature.movements.domain

import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.RemoteData
import com.nerdsyntax.juntalucas.core.format.MAX_MONEY
import kotlinx.coroutines.flow.Flow

enum class ExpenseType(val label: String) {
    FIXED("Fijo"), VARIABLE("Variable")
}

data class ExpenseDraft(
    val id: String,
    val userId: String,
    val description: String,
    val category: String,
    val amount: Long,
    val expenseDate: String,
    val type: ExpenseType,
    val paymentMethod: PaymentMethod,
    val note: String = ""
) {
    fun validate() {
        if (id.isBlank() || '/' in id || userId.isBlank()) throw DataValidationException("Tu sesión no es válida.")
        if (description.isBlank() || description.length > 200) throw DataValidationException("Ingresa una descripción válida.")
        if (category.isBlank() || category.length > 100) throw DataValidationException("Ingresa una categoría válida.")
        if (!SaleDates.valid(expenseDate)) throw DataValidationException("Selecciona una fecha válida.")
        if (amount !in 1..MAX_MONEY) throw DataValidationException("El monto debe ser mayor que cero en pesos CLP.")
        if (note.length > 2000) throw DataValidationException("La nota admite hasta 2000 caracteres.")
    }
}

data class Expense(val draft: ExpenseDraft, val createdAtMillis: Long)

interface ExpensesRepository {
    fun observeMonth(uid: String, month: String): Flow<Result<RemoteData<List<Expense>>>>
    fun observeRange(uid: String, startDate: String, endDateExclusive: String): Flow<Result<RemoteData<List<Expense>>>> = observeMonth(uid, startDate.take(7))
    suspend fun save(draft: ExpenseDraft): Result<Unit>
}

data class ExpensesSummary(val total: Long, val average: Long)

fun summarizeExpenses(expenses: List<Expense>): ExpensesSummary {
    val total = expenses.fold(0L) { sum, item -> Math.addExact(sum, item.draft.amount) }
    if (expenses.isEmpty()) return ExpensesSummary(0, 0)
    val count = expenses.size.toLong()
    return ExpensesSummary(total, total / count + if ((total % count) * 2 >= count) 1 else 0)
}
