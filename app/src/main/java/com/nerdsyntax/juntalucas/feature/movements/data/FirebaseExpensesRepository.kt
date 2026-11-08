package com.nerdsyntax.juntalucas.feature.movements.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.firestoreResult
import com.nerdsyntax.juntalucas.core.data.observeOwned
import com.nerdsyntax.juntalucas.core.data.requireOwner
import com.nerdsyntax.juntalucas.feature.movements.domain.Expense
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpenseDraft
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpenseType
import com.nerdsyntax.juntalucas.feature.movements.domain.ExpensesRepository
import com.nerdsyntax.juntalucas.feature.movements.domain.PaymentMethod
import com.nerdsyntax.juntalucas.feature.movements.domain.SaleDates
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseExpensesRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ExpensesRepository {

    private fun expenses(uid: String) = firestore.collection("users")
        .document(uid)
        .collection("expenses")

    override fun observeHistory(uid: String) = expenses(uid)
        .orderBy("expenseDate", Query.Direction.DESCENDING)
        .observeOwned(auth, uid) { it.toExpense(uid) }

    override fun observeMonth(uid: String, month: String) =
        observeRange(uid, "$month-01", "${SaleDates.shiftMonth(month, 1)}-01")

    override fun observeRange(
        uid: String,
        startDate: String,
        endDateExclusive: String
    ) = expenses(uid)
        .whereGreaterThanOrEqualTo("expenseDate", startDate)
        .whereLessThan("expenseDate", endDateExclusive)
        .orderBy("expenseDate", Query.Direction.DESCENDING)
        .observeOwned(auth, uid) { it.toExpense(uid) }

    override suspend fun save(draft: ExpenseDraft): Result<Unit> = firestoreResult {
        draft.validate()
        auth.requireOwner(draft.userId)
        val reference = expenses(draft.userId).document(draft.id)

        firestore.runTransaction { transaction ->
            auth.requireOwner(draft.userId)
            val existing = transaction.get(reference)

            if (existing.exists()) {
                if (existing.toExpense(draft.userId).draft != draft) {
                    throw DataValidationException(
                        "Este gasto ya fue guardado con otros datos. " +
                                "Revisa el listado antes de registrar otro."
                    )
                }
            } else {
                transaction.set(
                    reference,
                    mapOf(
                        "id" to draft.id,
                        "userId" to draft.userId,
                        "description" to draft.description,
                        "category" to draft.category,
                        "amount" to draft.amount,
                        "expenseDate" to draft.expenseDate,
                        "type" to draft.type.name,
                        "paymentMethod" to draft.paymentMethod.name,
                        "note" to draft.note,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            Unit
        }.await()

        auth.requireOwner(draft.userId)
    }

    override suspend fun getById(uid: String, expenseId: String): Result<Expense> {
        return try {
            val reference = expenseReference(uid, expenseId)
            val snapshot = reference.get().await()
            auth.requireOwner(uid)
            Result.success(requireExistingExpense(snapshot, uid))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun update(draft: ExpenseDraft): Result<Unit> = firestoreResult {
        draft.validate()
        val reference = expenseReference(draft.userId, draft.id)

        firestore.runTransaction { transaction ->
            auth.requireOwner(draft.userId)
            requireExistingExpense(transaction.get(reference), draft.userId)

            transaction.update(
                reference,
                mapOf(
                    "description" to draft.description,
                    "category" to draft.category,
                    "amount" to draft.amount,
                    "expenseDate" to draft.expenseDate,
                    "type" to draft.type.name,
                    "paymentMethod" to draft.paymentMethod.name,
                    "note" to draft.note
                )
            )

            Unit
        }.await()

        auth.requireOwner(draft.userId)
    }

    override suspend fun delete(uid: String, expenseId: String): Result<Unit> =
        firestoreResult {
            val reference = expenseReference(uid, expenseId)

            firestore.runTransaction { transaction ->
                auth.requireOwner(uid)
                requireExistingExpense(transaction.get(reference), uid)
                transaction.delete(reference)
                Unit
            }.await()

            auth.requireOwner(uid)
        }

    private fun expenseReference(uid: String, expenseId: String): DocumentReference {
        auth.requireOwner(uid)

        if (expenseId.isBlank() || '/' in expenseId) {
            throw DataValidationException("El gasto no es válido.")
        }

        return expenses(uid).document(expenseId)
    }

    private fun requireExistingExpense(snapshot: DocumentSnapshot, uid: String): Expense {
        if (!snapshot.exists()) {
            throw DataValidationException("Este gasto ya no existe.")
        }

        return snapshot.toExpense(uid)
    }
}

internal fun DocumentSnapshot.toExpense(uid: String): Expense {
    val draft = ExpenseDraft(
        id = id,
        userId = getString("userId") ?: error("Missing owner"),
        description = getString("description") ?: error("Missing description"),
        category = getString("category") ?: error("Missing category"),
        amount = getLong("amount") ?: error("Missing amount"),
        expenseDate = getString("expenseDate") ?: error("Missing date"),
        type = ExpenseType.valueOf(getString("type") ?: error("Missing type")),
        paymentMethod = PaymentMethod.valueOf(
            getString("paymentMethod") ?: error("Missing payment")
        ),
        note = getString("note").orEmpty()
    )

    draft.validate()
    check(draft.userId == uid && getString("id") == id) { "Invalid expense" }

    return Expense(
        draft = draft,
        createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L
    )
}
