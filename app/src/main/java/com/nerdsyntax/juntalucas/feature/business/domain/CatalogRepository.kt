package com.nerdsyntax.juntalucas.feature.business.domain

import com.nerdsyntax.juntalucas.core.data.RemoteData
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.format.MAX_MONEY
import com.nerdsyntax.juntalucas.core.format.MAX_QUANTITY
import kotlinx.coroutines.flow.Flow

data class CatalogProduct(
    val id: String,
    val userId: String,
    val name: String,
    val isProduct: Boolean,
    val price: Long,
    val unitCost: Long?,
    val active: Boolean = true,
    val trackStock: Boolean = false,
    val stock: Long = 0,
    val category: String = "",
    val description: String = "",
    val stockMinimum: Long = 0,
    val unit: String = "unidad",
    val isAsset: Boolean = false
)

interface CatalogRepository {
    fun observeActive(uid: String): Flow<Result<RemoteData<List<CatalogProduct>>>>
    fun observeAll(uid: String): Flow<Result<RemoteData<List<CatalogProduct>>>>
    suspend fun create(product: CatalogProduct): Result<Unit>
    suspend fun update(product: CatalogProduct): Result<Unit>
}

fun validateProduct(product: CatalogProduct) {
    if (product.id.isBlank() || '/' in product.id || product.userId.isBlank() || product.name.isBlank() || product.name.length > 200)
        throw DataValidationException("Ingresa un nombre válido (hasta 200 caracteres).")
    if (product.price !in 1..MAX_MONEY || (product.unitCost != null && product.unitCost !in 0..MAX_MONEY))
        throw DataValidationException("Precio y costo deben ser pesos enteros válidos; el precio debe ser positivo.")
    if (product.stock !in 0..MAX_QUANTITY || product.stockMinimum !in 0..MAX_QUANTITY || (!product.isProduct && (product.trackStock || product.stock != 0L)))
        throw DataValidationException("Revisa las existencias del producto.")
    if (product.description.length > 2000 || product.category.length > 100 || product.unit.length > 50)
        throw DataValidationException("La descripción, categoría o unidad es demasiado larga.")
}
