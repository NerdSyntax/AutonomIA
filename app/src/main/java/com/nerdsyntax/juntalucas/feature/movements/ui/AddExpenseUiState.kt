package com.nerdsyntax.juntalucas.feature.movements.ui

import com.nerdsyntax.juntalucas.feature.movements.domain.ExpenseType
import com.nerdsyntax.juntalucas.feature.movements.domain.PaymentMethod
import com.nerdsyntax.juntalucas.feature.movements.domain.SaleDates

data class AddExpenseUiState(
    val description: String = "",
    val category: String = "",
    val amount: String = "",
    val date: String = SaleDates.today(),
    val type: ExpenseType = ExpenseType.VARIABLE,
    val paymentMethod: PaymentMethod? = null,
    val note: String = "",
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
