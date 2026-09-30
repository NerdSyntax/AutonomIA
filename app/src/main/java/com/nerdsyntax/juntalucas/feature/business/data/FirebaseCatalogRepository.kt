package com.nerdsyntax.juntalucas.feature.business.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.firestoreResult
import com.nerdsyntax.juntalucas.core.data.observeOwned
import com.nerdsyntax.juntalucas.core.data.requireOwner
import com.nerdsyntax.juntalucas.feature.business.domain.validateProduct

import com.nerdsyntax.juntalucas.feature.business.domain.CatalogProduct
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogRepository
import kotlinx.coroutines.tasks.await

class FirebaseCatalogRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CatalogRepository {
    override fun observeActive(uid: String) = firestore.collection("users").document(uid)
        .collection("products").whereEqualTo("active", true).observeOwned(auth, uid) { it.toCatalogProduct(uid) }

    override fun observeAll(uid: String) = firestore.collection("users").document(uid)
        .collection("products").observeOwned(auth, uid) { it.toCatalogProduct(uid) }

    override suspend fun create(product: CatalogProduct): Result<Unit> = firestoreResult {
        saveInternal(product, allowExisting = false)
    }

    override suspend fun update(product: CatalogProduct): Result<Unit> = firestoreResult {
        saveInternal(product, allowExisting = true)
    }

    private suspend fun saveInternal(product: CatalogProduct, allowExisting: Boolean) {
        auth.requireOwner(product.userId)
        validateProduct(product)
        val reference = firestore.collection("users").document(product.userId).collection("products").document(product.id)
        firestore.runTransaction { transaction ->
            auth.requireOwner(product.userId)
            val existing = transaction.get(reference)
            if (existing.exists()) {
                if (!allowExisting || existing.toCatalogProduct(product.userId).id != product.id)
                    throw DataValidationException("Este producto ya se guardó. Vuelve al catálogo antes de crear otro.")
                transaction.update(reference, mapOf(
                    "name" to product.name, "isProduct" to product.isProduct, "price" to product.price,
                    "unitCost" to product.unitCost, "active" to product.active, "trackStock" to product.trackStock,
                    "stock" to product.stock, "category" to product.category, "description" to product.description,
                    "stockMinimum" to product.stockMinimum, "unit" to product.unit, "isAsset" to product.isAsset
                ))
            } else {
                transaction.set(reference, mapOf(
                    "id" to product.id, "userId" to product.userId, "name" to product.name,
                    "isProduct" to product.isProduct, "price" to product.price, "unitCost" to product.unitCost,
                    "active" to product.active, "trackStock" to product.trackStock, "stock" to product.stock,
                    "category" to product.category, "description" to product.description,
                    "stockMinimum" to product.stockMinimum, "unit" to product.unit,
                    "isAsset" to product.isAsset,
                    "createdAt" to FieldValue.serverTimestamp()
                ))
            }
            Unit
        }.await()
        auth.requireOwner(product.userId)
    }
}

internal fun DocumentSnapshot.toCatalogProduct(uid: String): CatalogProduct {
    val product = CatalogProduct(
        id = id, userId = getString("userId") ?: error("Missing owner"),
        name = getString("name") ?: error("Missing name"),
        isProduct = getBoolean("isProduct") ?: error("Missing type"),
        price = getLong("price") ?: error("Missing price"), unitCost = getLong("unitCost"),
        active = getBoolean("active") ?: error("Missing status"),
        trackStock = getBoolean("trackStock") ?: error("Missing stock policy"),
        stock = getLong("stock") ?: error("Missing stock"), category = getString("category").orEmpty(),
        description = getString("description").orEmpty(), stockMinimum = getLong("stockMinimum") ?: 0,
        unit = getString("unit") ?: "unidad", isAsset = getBoolean("isAsset") ?: false
    )
    check(product.userId == uid && getString("id") == id) { "Invalid owner or ID" }
    validateProduct(product)
    return product
}
