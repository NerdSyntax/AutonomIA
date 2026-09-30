package com.nerdsyntax.juntalucas.feature.movements.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.firestoreResult
import com.nerdsyntax.juntalucas.core.data.observeOwned
import com.nerdsyntax.juntalucas.core.data.requireOwner
import com.nerdsyntax.juntalucas.feature.business.data.toCatalogProduct
import com.nerdsyntax.juntalucas.feature.movements.domain.*
import kotlinx.coroutines.tasks.await

class FirebaseSalesRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SalesRepository {
    private fun sales(uid: String) = firestore.collection("users").document(uid).collection("sales")

    override fun observeMonth(uid: String, month: String) = observeRange(uid, "$month-01", "${SaleDates.shiftMonth(month, 1)}-01")

    override fun observeRange(uid: String, startDate: String, endDateExclusive: String) = sales(uid)
        .whereGreaterThanOrEqualTo("saleDate", startDate)
        .whereLessThan("saleDate", endDateExclusive)
        .orderBy("saleDate", Query.Direction.DESCENDING)
        .observeOwned(auth, uid) { it.toSale(uid) }

    override suspend fun save(draft: SaleDraft): Result<Unit> = firestoreResult {
        draft.validate()
        auth.requireOwner(draft.userId)
        val saleRef = sales(draft.userId).document(draft.id)
        val productRef = firestore.collection("users").document(draft.userId).collection("products").document(draft.productId)
        firestore.runTransaction { transaction ->
            auth.requireOwner(draft.userId)
            val existing = transaction.get(saleRef)
            if (existing.exists()) {
                // A retry of this exact request is already committed. Never decrement stock twice.
                if (existing.toSale(draft.userId).draft != draft)
                    throw DataValidationException("Esta venta ya fue guardada con otros datos. Revisa el listado antes de registrar otra.")
            } else {
                val snapshot = transaction.get(productRef)
                if (!snapshot.exists()) throw DataValidationException("El producto ya no existe. Selecciona otro.")
                val product = snapshot.toCatalogProduct(draft.userId)
                if (!product.active) throw DataValidationException("El producto ya no está activo.")
                val tracksStock = product.isProduct && product.trackStock
                if (tracksStock && product.stock < draft.quantity)
                    throw DataValidationException("Stock insuficiente: quedan ${product.stock} unidades.")
                transaction.set(saleRef, mapOf(
                    "id" to draft.id, "userId" to draft.userId, "saleDate" to draft.saleDate,
                    "createdAt" to FieldValue.serverTimestamp(), "productId" to draft.productId,
                    "productName" to product.name, "quantity" to draft.quantity, "unitPrice" to draft.unitPrice,
                    "unitCost" to product.unitCost, "discount" to draft.discount, "total" to draft.total,
                    "paymentMethod" to draft.paymentMethod.name, "note" to draft.note, "stockTracked" to tracksStock
                ))
                if (tracksStock) transaction.update(productRef, mapOf(
                    "stock" to product.stock - draft.quantity, "lastSaleId" to draft.id
                ))
            }
            Unit
        }.await()
        auth.requireOwner(draft.userId)
    }
}

internal fun DocumentSnapshot.toSale(uid: String): Sale {
    val draft = SaleDraft(
        id = id, userId = getString("userId") ?: error("Missing owner"),
        saleDate = getString("saleDate") ?: error("Missing date"),
        productId = getString("productId") ?: error("Missing product"),
        quantity = getLong("quantity") ?: error("Missing quantity"),
        unitPrice = getLong("unitPrice") ?: error("Missing price"),
        discount = getLong("discount") ?: error("Missing discount"),
        paymentMethod = PaymentMethod.valueOf(getString("paymentMethod") ?: error("Missing payment")),
        note = getString("note").orEmpty()
    )
    draft.validate()
    check(draft.userId == uid && getString("id") == id && getLong("total") == draft.total) { "Invalid sale" }
    return Sale(draft, getString("productName") ?: error("Missing name"), getLong("unitCost"),
        // Local pending writes have a null server timestamp; the confirmed event
        // replaces it without turning a temporarily cached sale into an error.
        getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        getBoolean("stockTracked") ?: error("Missing stock policy"))
}
