package com.nerdsyntax.juntalucas.feature.business.domain

import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.RemoteData
import com.nerdsyntax.juntalucas.core.format.MAX_MONEY
import kotlinx.coroutines.flow.Flow

data class ReusableExpense(
    val id: String,
    val userId: String,
    val name: String,
    val category: String,
    val suggestedAmount: Long? = null
) {
    fun validate() {
        if (id.isBlank() || '/' in id || userId.isBlank()) {
            throw DataValidationException("El gasto reutilizable no es válido.")
        }

        if (name.isBlank() || name.length > 200) {
            throw DataValidationException("Ingresa un nombre de hasta 200 caracteres.")
        }

        if (category.isBlank() || category.length > 100) {
            throw DataValidationException("Ingresa una categoría de hasta 100 caracteres.")
        }

        if (suggestedAmount != null && suggestedAmount !in 1L..MAX_MONEY) {
            throw DataValidationException(
                "Ingresa un monto sugerido válido en CLP o déjalo vacío."
            )
        }
    }
}

interface ReusableExpensesRepository {
    fun observeAll(uid: String): Flow<Result<RemoteData<List<ReusableExpense>>>>

    suspend fun create(expense: ReusableExpense): Result<Unit>

    suspend fun update(expense: ReusableExpense): Result<Unit>
}