package com.nerdsyntax.juntalucas.feature.movements.ui

import com.nerdsyntax.juntalucas.core.format.parseClp
import com.nerdsyntax.juntalucas.core.format.parseQuantity
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogProduct
import com.nerdsyntax.juntalucas.feature.movements.domain.*

data class AddSaleUiState(
    val date: String = SaleDates.today(),
    val productId: String = "",
    val quantity: String = "1",
    val unitPrice: String = "",
    val discount: String = "",
    val paymentMethod: PaymentMethod? = null,
    val note: String = "",
    val products: List<CatalogProduct> = emptyList(),
    val isCatalogLoading: Boolean = true,
    val catalogError: String? = null,
    val catalogFromCache: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedProduct: CatalogProduct? get() = products.find { it.id == productId }
    val total: Long? get() = runCatching {
        calculateSaleTotal(parseQuantity(quantity) ?: return null, parseClp(unitPrice) ?: return null,
            if (discount.isBlank()) 0 else parseClp(discount) ?: return null)
    }.getOrNull()
}
