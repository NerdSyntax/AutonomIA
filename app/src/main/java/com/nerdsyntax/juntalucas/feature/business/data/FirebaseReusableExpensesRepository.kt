package com.nerdsyntax.juntalucas.feature.business.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.firestoreResult
import com.nerdsyntax.juntalucas.core.data.observeOwned
import com.nerdsyntax.juntalucas.core.data.requireOwner
import com.nerdsyntax.juntalucas.feature.business.domain.ReusableExpense
import com.nerdsyntax.juntalucas.feature.business.domain.ReusableExpensesRepository
import kotlinx.coroutines.tasks.await

class FirebaseReusableExpensesRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ReusableExpensesRepository {

    private fun reusableExpenses(uid: String) =
        firestore.collection("users")
            .document(uid)
            .collection("reusableExpenses")

    override fun observeAll(uid: String) =
        reusableExpenses(uid)
            .orderBy("name")
            .observeOwned(auth, uid) { snapshot ->
                snapshot.toReusableExpense(uid)
            }

    override suspend fun create(expense: ReusableExpense): Result<Unit> =
        firestoreResult {
            expense.validate()
            auth.requireOwner(expense.userId)

            val reference = reusableExpenses(expense.userId).document(expense.id)

            firestore.runTransaction { transaction ->
                auth.requireOwner(expense.userId)
                val existing = transaction.get(reference)

                if (existing.exists()) {
                    if (existing.toReusableExpense(expense.userId) != expense) {
                        throw DataValidationException(
                            "Ya existe un gasto reutilizable con ese identificador."
                        )
                    }
                } else {
                    transaction.set(
                        reference,
                        expense.editableFields() + mapOf(
                            "id" to expense.id,
                            "userId" to expense.userId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                Unit
            }.await()

            auth.requireOwner(expense.userId)
        }

    override suspend fun update(expense: ReusableExpense): Result<Unit> =
        firestoreResult {
            expense.validate()
            auth.requireOwner(expense.userId)

            val reference = reusableExpenses(expense.userId).document(expense.id)

            firestore.runTransaction { transaction ->
                auth.requireOwner(expense.userId)
                val existing = transaction.get(reference)

                if (!existing.exists()) {
                    throw DataValidationException(
                        "Este gasto reutilizable ya no existe."
                    )
                }

                existing.toReusableExpense(expense.userId)
                transaction.update(reference, expense.editableFields())

                Unit
            }.await()

            auth.requireOwner(expense.userId)
        }

    private fun ReusableExpense.editableFields(): Map<String, Any?> = mapOf(
        "name" to name,
        "category" to category,
        "suggestedAmount" to suggestedAmount
    )

    private fun DocumentSnapshot.toReusableExpense(uid: String): ReusableExpense {
        val owner = getString("userId")

        if (owner != uid || getString("id") != id) {
            throw DataValidationException("No se pudo verificar el gasto reutilizable.")
        }

        return ReusableExpense(
            id = id,
            userId = uid,
            name = getString("name").orEmpty(),
            category = getString("category").orEmpty(),
            suggestedAmount = getLong("suggestedAmount")
        ).also { it.validate() }
    }
}