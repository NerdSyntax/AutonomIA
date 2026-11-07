package com.nerdsyntax.juntalucas.feature.business.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nerdsyntax.juntalucas.core.data.DataValidationException
import com.nerdsyntax.juntalucas.core.data.firestoreResult
import com.nerdsyntax.juntalucas.core.data.observeOwned
import com.nerdsyntax.juntalucas.core.data.requireOwner
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogProduct
import com.nerdsyntax.juntalucas.feature.business.domain.CatalogRepository
import com.nerdsyntax.juntalucas.feature.business.domain.validateProduct
import kotlinx.coroutines.tasks.await

class FirebaseCatalogRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CatalogRepository {

    override fun observeActive(uid: String) = firestore.collection("users")
        .document(uid)
        .collection("products")
        .whereEqualTo("active", true)
        .observeOwned(auth, uid) { it.toCatalogProduct(uid) }

    override fun observeAll(uid: String) = firestore.collection("users")
        .document(uid)
        .collection("products")
        .observeOwned(auth, uid) { it.toCatalogProduct(uid) }

    override suspend fun create(product: CatalogProduct): Result<Unit> = firestoreResult {
        saveInternal(product, allowExisting = false)
    }

    override suspend fun update(product: CatalogProduct): Result<Unit> = firestoreResult {
        saveInternal(product, allowExisting = true)
    }

    override suspend fun updateWithPhotoChange(product: CatalogProduct): Result<Unit> =
        firestoreResult {
            saveInternal(product, allowExisting = true, changePhoto = true)
        }

    private suspend fun saveInternal(
        product: CatalogProduct,
        allowExisting: Boolean,
        changePhoto: Boolean = false
    ) {
        auth.requireOwner(product.userId)
        validateProduct(product)

        val newPhotoPath = product.photoStoragePath
        if (changePhoto && newPhotoPath != null && !isProductPhotoPath(product, newPhotoPath)) {
            throw DataValidationException("La foto seleccionada no corresponde a este producto.")
        }

        val reference = firestore.collection("users")
            .document(product.userId)
            .collection("products")
            .document(product.id)

        val fields = mapOf<String, Any?>(
            "name" to product.name,
            "isProduct" to product.isProduct,
            "price" to product.price,
            "unitCost" to product.unitCost,
            "active" to product.active,
            "trackStock" to product.trackStock,
            "stock" to product.stock,
            "category" to product.category,
            "description" to product.description,
            "stockMinimum" to product.stockMinimum,
            "unit" to product.unit,
            "isAsset" to product.isAsset
        )

        val previousPhotoPath = firestore.runTransaction { transaction ->
            auth.requireOwner(product.userId)
            val existing = transaction.get(reference)

            if (existing.exists()) {
                if (!allowExisting || existing.toCatalogProduct(product.userId).id != product.id) {
                    throw DataValidationException(
                        "Este producto ya se guardó. Vuelve al catálogo antes de crear otro."
                    )
                }

                val changes = fields.toMutableMap()
                val oldPhotoPath = existing.getString("photoStoragePath")

                if (changePhoto) {
                    changes["photoStoragePath"] = newPhotoPath ?: FieldValue.delete()
                }

                transaction.update(reference, changes)

                if (changePhoto && oldPhotoPath != newPhotoPath) {
                    oldPhotoPath.orEmpty()
                } else {
                    ""
                }
            } else {
                if (changePhoto) {
                    throw DataValidationException("El producto ya no existe.")
                }

                val newFields = fields.toMutableMap()
                if (newPhotoPath != null) {
                    newFields["photoStoragePath"] = newPhotoPath
                }

                transaction.set(
                    reference,
                    newFields + mapOf(
                        "id" to product.id,
                        "userId" to product.userId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )

                ""
            }
        }.await()

        auth.requireOwner(product.userId)

        // Firestore ya confirmó el cambio. Ahora es seguro limpiar la foto anterior.
        if (isProductPhotoPath(product, previousPhotoPath)) {
            deletePreviousPhoto(previousPhotoPath)
        }
    }

    private fun isProductPhotoPath(product: CatalogProduct, path: String): Boolean {
        val folder = "users/${product.userId}/productPhotos/${product.id}/"
        if (!path.startsWith(folder)) return false

        val fileName = path.removePrefix(folder)
        return fileName.length > 4 && '/' !in fileName && fileName.endsWith(".jpg")
    }

    private fun deletePreviousPhoto(path: String) {
        try {
            FirebaseStorage.getInstance()
                .reference
                .child(path)
                .delete()
                .addOnFailureListener { error ->
                    Log.w("ProductPhoto", "No se pudo borrar la foto anterior de Storage.", error)
                }
        } catch (error: Exception) {
            Log.w("ProductPhoto", "No se pudo iniciar el borrado de la foto anterior.", error)
        }
    }
}

internal fun DocumentSnapshot.toCatalogProduct(uid: String): CatalogProduct {
    val product = CatalogProduct(
        id = id,
        userId = getString("userId") ?: error("Missing owner"),
        name = getString("name") ?: error("Missing name"),
        isProduct = getBoolean("isProduct") ?: error("Missing type"),
        price = getLong("price") ?: error("Missing price"),
        unitCost = getLong("unitCost"),
        active = getBoolean("active") ?: error("Missing status"),
        trackStock = getBoolean("trackStock") ?: error("Missing stock policy"),
        stock = getLong("stock") ?: error("Missing stock"),
        category = getString("category").orEmpty(),
        description = getString("description").orEmpty(),
        stockMinimum = getLong("stockMinimum") ?: 0,
        unit = getString("unit") ?: "unidad",
        isAsset = getBoolean("isAsset") ?: false,
        photoStoragePath = getString("photoStoragePath")
    )

    check(product.userId == uid && getString("id") == id) {
        "Invalid owner or ID"
    }

    validateProduct(product)
    return product
}