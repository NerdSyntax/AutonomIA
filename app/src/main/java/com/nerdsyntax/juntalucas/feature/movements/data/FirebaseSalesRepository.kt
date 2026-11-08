package com.nerdsyntax.juntalucas.feature.movements.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.firestoreResult
import com.nerdsyntax.juntalucas.core.data.observeOwned
import com.nerdsyntax.juntalucas.core.data.requireOwner
import com.nerdsyntax.juntalucas.feature.business.data.toCatalogProduct
import com.nerdsyntax.juntalucas.feature.movements.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseSalesRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SalesRepository {

    private fun sales(uid: String) = firestore.collection("users").document(uid).collection("sales")

    private fun productRef(uid: String, productId: String) =
        firestore.collection("users").document(uid).collection("products").document(productId)

    override fun observeMonth(uid: String, month: String) =
        observeRange(uid, "$month-01", "${SaleDates.shiftMonth(month, 1)}-01")

    override fun observeRange(uid: String, startDate: String, endDateExclusive: String) = sales(uid)
        .whereGreaterThanOrEqualTo("saleDate", startDate)
        .whereLessThan("saleDate", endDateExclusive)
        .orderBy("saleDate", Query.Direction.DESCENDING)
        .observeOwned(auth, uid) { it.toSale(uid) }

    override suspend fun save(draft: SaleDraft): Result<Unit> = firestoreResult {
        draft.validate()
        auth.requireOwner(draft.userId)
        val saleRef = sales(draft.userId).document(draft.id)
        val productRef = productRef(draft.userId, draft.productId)

        firestore.runTransaction { transaction ->
            auth.requireOwner(draft.userId)
            val existing = transaction.get(saleRef)
            if (existing.exists()) {
                // Retrying a committed request must not deduct inventory again.
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

    override suspend fun getById(uid: String, saleId: String): Result<Sale> = try {
        val snapshot = saleRef(uid, saleId).get().await()
        auth.requireOwner(uid)
        Result.success(requireSale(snapshot, uid))
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun update(draft: SaleDraft): Result<Unit> = firestoreResult {
        draft.validate()
        val reference = saleRef(draft.userId, draft.id)

        firestore.runTransaction { transaction ->
            auth.requireOwner(draft.userId)
            val previous = requireSale(transaction.get(reference), draft.userId)
            if (previous.draft.productId != draft.productId) {
                throw DataValidationException("No puedes cambiar el producto de una venta ya registrada.")
            }

            if (previous.draft != draft) {
                val stockDelta = previous.draft.quantity - draft.quantity
                if (previous.stockTracked && stockDelta != 0L) {
                    adjustStock(transaction, previous, stockDelta)
                }
                transaction.update(reference, mapOf(
                    "saleDate" to draft.saleDate,
                    "quantity" to draft.quantity,
                    "unitPrice" to draft.unitPrice,
                    "discount" to draft.discount,
                    "total" to draft.total,
                    "paymentMethod" to draft.paymentMethod.name,
                    "note" to draft.note
                ))
            }
            Unit
        }.await()

        auth.requireOwner(draft.userId)
    }

    override suspend fun delete(uid: String, saleId: String): Result<Unit> = firestoreResult {
        val reference = saleRef(uid, saleId)

        firestore.runTransaction { transaction ->
            auth.requireOwner(uid)
            val previous = requireSale(transaction.get(reference), uid)
            if (previous.stockTracked) {
                adjustStock(transaction, previous, previous.draft.quantity)
            }
            transaction.delete(reference)
            Unit
        }.await()

        auth.requireOwner(uid)
    }

    private fun saleRef(uid: String, saleId: String): DocumentReference {
        auth.requireOwner(uid)
        if (saleId.isBlank() || '/' in saleId || saleId == "." || saleId == "..") {
            throw DataValidationException("La venta no es válida.")
        }
        return sales(uid).document(saleId)
    }

    private fun requireSale(snapshot: DocumentSnapshot, uid: String): Sale {
        if (!snapshot.exists()) throw DataValidationException("Esta venta ya no existe.")
        return snapshot.toSale(uid)
    }

    private fun adjustStock(transaction: Transaction, sale: Sale, delta: Long) {
        val reference = productRef(sale.draft.userId, sale.draft.productId)
        val snapshot = transaction.get(reference)
        if (!snapshot.exists() || snapshot.getString("userId") != sale.draft.userId) {
            throw DataValidationException("No se encontró el producto para ajustar su stock.")
        }
        val stock = snapshot.getLong("stock")?.takeIf { it >= 0L }
            ?: throw DataValidationException("El producto no tiene un stock válido.")
        val nextStock = try {
            Math.addExact(stock, delta)
        } catch (_: ArithmeticException) {
            throw DataValidationException("El stock supera el máximo permitido.")
        }
        if (nextStock < 0L) {
            throw DataValidationException("Stock insuficiente: quedan $stock unidades adicionales.")
        }
        transaction.update(reference, mapOf(
            "stock" to nextStock,
            "lastSaleId" to sale.draft.id
        ))
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
    return Sale(
        draft, getString("productName") ?: error("Missing name"), getLong("unitCost"),
        // A local pending write may not have its server timestamp yet.
        getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        getBoolean("stockTracked") ?: error("Missing stock policy")
    )
}
